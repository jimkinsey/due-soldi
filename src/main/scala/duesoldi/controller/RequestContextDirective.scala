package duesoldi.controller

import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract

case class RequestContext(id: UUID)

object RequestContextDirective {
  def inContext: Directive1[RequestContext] = extract(_ => RequestContext(id = UUID.randomUUID()))
}
