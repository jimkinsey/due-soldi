package duesoldi.controller

import java.util.UUID

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Config
import duesoldi.controller.WithConfigDirective.withConfig

case class RequestContext(id: UUID, config: Config)

object RequestContextDirective
{
  def inContext(appConfig: Config): Directive[Tuple1[RequestContext]] =
    withConfig(appConfig).flatMap { reqConfig =>
      val context = RequestContext(id = UUID.randomUUID(), reqConfig)
      extract(_ => context) & mapResponse(_.addHeader(RawHeader("Request-ID", context.id.toString)))
    }
}
