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
    def dependencies[DEP1,DEP2](implicit dep1: Inject[DEP1], dep2: Inject[DEP2]): Into[(DEP1,DEP2)] = {
      dependency((ctx) => (dep1(ctx),dep2(ctx)))
    }
    def dependencies[DEP1,DEP2,DEP3](implicit dep1: Inject[DEP1], dep2: Inject[DEP2], dep3: Inject[DEP3]): Into[(DEP1,DEP2,DEP3)] = {
      dependency((ctx) => (dep1(ctx),dep2(ctx),dep3(ctx)))
    }
    def dependencies[DEP1,DEP2,DEP3,DEP4](implicit dep1: Inject[DEP1], dep2: Inject[DEP2], dep3: Inject[DEP3], dep4: Inject[DEP4]): Into[(DEP1,DEP2,DEP3,DEP4)] = {
      dependency((ctx) => (dep1(ctx),dep2(ctx),dep3(ctx),dep4(ctx)))
    }
  }
  def inject(implicit appConfig: Config): RequestDependencyInjector = new RequestDependencyInjector
}
