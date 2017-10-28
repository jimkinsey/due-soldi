package duesoldi.controller

import akka.http.scaladsl.server.Directives.extract
import akka.http.scaladsl.server.{Directive, Directive1}
import duesoldi.config.Config
import duesoldi.controller.DependenciesDirective.withDependencies
import duesoldi.controller.RequestConfigDirective.requestConfig
import duesoldi.dependencies.Dependencies

object DependenciesDirective
{
  def withDependencies(config: Config): Directive1[Dependencies] =
    extract { ctx =>
      import ctx.executionContext
      new Dependencies(config)
    }
}

object RequestDependenciesDirective
{
  def withRequestDependencies(config: Config): Directive[Tuple1[Dependencies]] =
    requestConfig(config).flatMap(withDependencies)
}