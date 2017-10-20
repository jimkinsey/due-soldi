package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.extract
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.dependencies.RequestDependencies

object RequestDependenciesDirective {
  def withDependencies(config: Config)
                      (block: RequestDependencies => Route)
                      (implicit context: RequestContext): Route =
    extract { (ctx: server.RequestContext) =>
      import ctx.executionContext
      new RequestDependencies(config)
    } apply block
}
