package br.tioback.urlshortener

import akka.actor.Actor

sealed trait ConvertMsg
case class EncodeMsg(id : Long) extends ConvertMsg
case class DecodeMsg(key : String) extends ConvertMsg

class UrlConverterActor extends Actor with UrlConverter {
  def actorRefFactory = context
  
  def receive = {
    case EncodeMsg(id) => sender ! encode(id)
    case DecodeMsg(key) => sender ! decode(key)
  }
}

trait UrlConverter {

  val dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_," split ""
  val base = dictionary.length

  def encode(id: Long): String = {
    if (id == 0) dictionary(0) else {
      var result = ""
      var i = id
      while (i > 0) {
        result += dictionary((i % base).toInt)
        i = (i / base).toInt
      }

      (result split "" reverse) mkString
    }
  }

  def decode(key: String): Long = {
    var result = 0L;
    (key split "").foreach { x => result = result * base + dictionary.indexWhere(_ == x) }
    result
  }
}