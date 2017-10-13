package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import duesoldi._
import duesoldi.config.Configured
import duesoldi.controller.RequestContextDirective._
import duesoldi.dependencies.{AppDependencies, AppDependenciesImpl, RequestDependencies}
import BlogEntryRoutes.blogEntryRoutes

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Controller
  with Configured
  with AppDependencies
  with RequestDependenciesDirective
  with AccessRecording
  with FurnitureRoutes
  with MetricsRoutes
  with BlogIndexRoutes
  with RobotsRoutes
  with BlogEditingRoutes
  with DebugRoutes {

  implicit val appDependencies: AppDependenciesImpl = new AppDependenciesImpl(config)

  lazy val routes: Route =
    inContext { implicit requestContext: RequestContext =>
      withDependencies(requestContext) { requestDependencies =>
        recordAccess {
          furnitureRoutes ~
          blogIndexRoutes ~
          blogEntryRoutes(requestDependencies.makeEntryPage) ~
          metricsRoutes ~
          robotsRoutes ~
          blogEditingRoutes ~
          debugRoutes
        }
      }
    }

}
