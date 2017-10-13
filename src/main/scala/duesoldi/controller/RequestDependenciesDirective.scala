package duesoldi.controller

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract
import duesoldi.dependencies.{AppDependenciesImpl, RequestDependencies}

import scala.concurrent.ExecutionContext

trait RequestDependenciesDirective {
  implicit def executionContext: ExecutionContext
  implicit def appDependencies: AppDependenciesImpl

  def withDependencies(context: RequestContext): Directive1[RequestDependencies] = extract(_ => new RequestDependencies(appDependencies, context))
}
