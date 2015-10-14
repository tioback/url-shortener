package br.tioback.urlshortener

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import org.elasticsearch.index.get.GetField
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.indices.IndexAlreadyExistsException
import org.elasticsearch.index.engine.DocumentMissingException
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

sealed trait VaultMsg
case class InitVaultMsg() extends VaultMsg
case class AddUrlMsg(longUrl : String) extends VaultMsg
case class GetUrlMsg(shortUrl : String) extends VaultMsg

class UrlVaultActor(urlConverter: ActorRef) extends Actor with UrlVault {
  def actorRefFactory = context
  
  def receive = {
    case InitVaultMsg => { 
      init
      sender ! None
    }
    case AddUrlMsg(longUrl) => sender ! addUrl(longUrl, urlConverter)
    case GetUrlMsg(shortUrl) => sender ! getUrl(shortUrl, urlConverter)
  }
}

trait UrlVault {
	implicit val timeout = Timeout(10 seconds)
  private val settings = ImmutableSettings.settingsBuilder().put("script.inline", "on")
  private val client = ElasticClient.local(settings.build)
  private val iidBatchSize = 1
 
  private var currentIdOffset: Long = -1
  private var currentMaxId: Long = -1
  
  private val indexesDefinitions = create index "shortener" mappings (
    "sequence" as (
      "iid" typed LongType
    ),
    "urls" as (
      "shortUrl" typed StringType,
      "longUrl" typed StringType
    )
  )
  
  private val iidInitialization = index into "shortener" / "sequence" id 1 fields ( "iid" -> 0L )
  
  private val iidIncrement = update id 1 in "shortener" / "sequence"  script ("ctx._source.iid += " + iidBatchSize) fields "_source"

  private def incrementIndex: Long = {
    client.execute { iidIncrement }.await.getGetResult.getSource.get("iid").toString.toLong
  }
  
  private def burnNextId : Long = {
    if (currentIdOffset == -1 || currentIdOffset <= iidBatchSize) {
      currentMaxId = incrementIndex
      currentIdOffset = 0;
    }
    var result = currentMaxId - (iidBatchSize - currentIdOffset)
    currentIdOffset += 1
    result
  }
  
  def init: Unit = {
    try {
    	client.execute { indexesDefinitions }.await 
    	client.execute { iidInitialization }.await
    } catch { 
      case e: IndexAlreadyExistsException => println("Index already exists")
    } 
  }
  
  def addUrl(longUrl: String, urlConverter : ActorRef) : String = {
    val nextId = burnNextId
    val future = urlConverter ? EncodeMsg(nextId)
    val encodedId = Await.result(future, timeout.duration).asInstanceOf[String]  
    client.execute { index into "shortener" / "url" id nextId fields (
        "shortUrl" -> encodedId,
        "longUrl" -> longUrl
      ) 
    }
    encodedId
  }
  
  def getUrl(shortUrl : String, urlConverter : ActorRef) : String = {
    val future = urlConverter ? DecodeMsg(shortUrl)
		val decodedId = Await.result(future, timeout.duration).asInstanceOf[Long]
    val result = client.execute { get id decodedId from "shortener" / "url" }.await
    try {
      result.getSource.get("longUrl").toString()
    } catch {
      case e : Throwable => ""
    }
  }
}