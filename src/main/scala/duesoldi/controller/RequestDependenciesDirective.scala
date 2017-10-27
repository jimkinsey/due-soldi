package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract
import duesoldi.dependencies.RequestDependencies

object RequestDependenciesDirective
{
  def withDependencies(implicit context: RequestContext): Directive1[RequestDependencies] =
    extract { (ctx: server.RequestContext) =>
      import ctx.executionContext
      new RequestDependencies(context.config)
    }
}
