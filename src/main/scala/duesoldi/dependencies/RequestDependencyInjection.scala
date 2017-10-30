package duesoldi.dependencies

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Config
import duesoldi.controller.RequestConfigDirective.requestConfig
import duesoldi.dependencies.Injection.Inject

object RequestDependencyInjection
{
  class Into[DEP](implicit appConfig: Config, dep: Inject[DEP])
  {
    def into: Directive1[DEP] = requestConfig(appConfig).flatMap(conf => provide(dep(conf)))
  }
  class RequestDependencyInjector(implicit appConfig: Config)
  {
    def dependency[DEP](implicit dep: Inject[DEP]): Into[DEP] = new Into
  }
  def inject(implicit appConfig: Config): RequestDependencyInjector = new RequestDependencyInjector
}
