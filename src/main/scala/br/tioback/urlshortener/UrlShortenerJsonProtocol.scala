package br.tioback.urlshortener

import spray.json.DefaultJsonProtocol

object UrlShortenerJsonProtocol extends DefaultJsonProtocol {
  implicit val shortenRequestFormat = jsonFormat1(ShortenRequest)
  implicit val shortenResponseFormat = jsonFormat2(ShortenResponse)
  implicit val stretchResponseFormat = jsonFormat3(StretchResponse)
}

case class ShortenRequest(longUrl: String)
case class ShortenResponse(id: String, longUrl: String)
case class StretchResponse(id: String, longUrl: String, status: String)