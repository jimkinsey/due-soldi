package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.extract
import akka.http.scaladsl.server.Route
import duesoldi.dependencies.{AppDependencies, RequestDependencies}

object RequestDependenciesDirective {
  def withDependencies(block: RequestDependencies => Route)
                      (implicit context: RequestContext, appDependencies: AppDependencies): Route =
    extract { (ctx: server.RequestContext) =>
      import ctx.executionContext
      new RequestDependencies(appDependencies, context)
    } apply block
}
