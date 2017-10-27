package duesoldi.controller

import java.util.UUID

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._

object TaggedRequestDirective
{
  def tagRequest: Directive0 = {
    val requestId = RawHeader("Request-ID", UUID.randomUUID().toString)
    mapRequest(_.addHeader(requestId)) & mapResponse(_.addHeader(requestId))
  }
}
