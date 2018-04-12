package cicerone

case class Request(method: String, url: String, body: Option[String] = None)
