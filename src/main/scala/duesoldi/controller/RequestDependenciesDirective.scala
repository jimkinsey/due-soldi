package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract
import duesoldi.config.Config
import duesoldi.dependencies.RequestDependencies

object RequestDependenciesDirective
{
  def withDependencies(config: Config): Directive1[RequestDependencies] =
    extract { (ctx: server.RequestContext) =>
      import ctx.executionContext
      new RequestDependencies(config, ctx.request.headers.find(_.name == "Request-ID").map(_.value).getOrElse("~unidentified~"))
    }
}
