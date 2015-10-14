package br.tioback.urlshortener

import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import br.tioback.urlshortener.UrlShortenerJsonProtocol._
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import MediaTypes._

class UrlShortenerActor(val urlVault: ActorRef) extends Actor with UrlShortenerService {
  def actorRefFactory = context
  def receive = runRoute(shorten ~ stretch)
}

trait UrlShortenerService extends HttpService {
  implicit val timeout = Timeout(10 seconds)
  
  val urlVault : ActorRef

  val shorten = (pathPrefix("encurte") & path("url") & post) {
    headerValue({
      case x @ HttpHeaders.`Content-Type`(value) => Some(value)
      case default                               => None
    }) {
      header => header match {
        case ContentType(MediaType("application/json"), _) => {
          entity(as[ShortenRequest]) { req =>
            respondWithMediaType(`application/json`) {
              complete {
                val longUrl = req.longUrl
                val future = urlVault ? AddUrlMsg(longUrl)
                val shortUrl = Await.result(future, timeout.duration).asInstanceOf[String]
                ShortenResponse(fillUrl(shortUrl), longUrl)
              }
            }
          }
        }
        case default => {
          complete {
            HttpResponse(406);
          }
        }
      }
    }
  }

  val stretch = (path("url") & get) {
    parameter('shortUrl) { shortUrl =>
      respondWithMediaType(`application/json`) {
        complete {
          val future = urlVault ? GetUrlMsg(cleanUrl(shortUrl))
    		  val longUrl = Await.result(future, timeout.duration).asInstanceOf[String]
				  val status = if (longUrl == "") "404" else "OK"
          StretchResponse(shortUrl, longUrl, status)
        }
      }
    }
  }
  
  private def fillUrl(shortUrl: String) : String = {
    "http://chrdc.co/" + shortUrl
  }
  
  private def cleanUrl(shortUrl : String) : String = {
    if (shortUrl.matches("""http:\/\/chrdc\.co\/[^\/]{1,65}""")) {
      shortUrl.replaceFirst("""http:\/\/chrdc\.co\/""", "")
    } else {
      ""
    }
  }
}