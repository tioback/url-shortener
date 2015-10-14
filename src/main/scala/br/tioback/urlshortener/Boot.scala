package br.tioback.urlshortener

import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.Await

object Boot extends App {
  implicit val system = ActorSystem("url-shortener")

  val converter = system.actorOf(Props[UrlConverterActor], "url-converter")
  val vault = system.actorOf(Props(new UrlVaultActor(converter)), "url-vault")
  val service = system.actorOf(Props(new UrlShortenerActor(vault)), "url-shortener-service")

  implicit val timeout = Timeout(10.seconds)

  Await.result(vault ? InitVaultMsg, timeout.duration)

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
