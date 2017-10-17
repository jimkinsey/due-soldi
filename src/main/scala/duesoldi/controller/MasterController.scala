package duesoldi.controller

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import duesoldi._
import duesoldi.config.Configured
import duesoldi.controller.BlogEntryRoutes.blogEntryRoutes
import duesoldi.controller.BlogIndexRoutes.blogIndexRoutes
import duesoldi.controller.FurnitureRoutes.furnitureRoutes
import MetricsRoutes.metricsRoutes
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.controller.DebugRoutes.debugRoutes
import duesoldi.controller.BlogEditingRoutes.blogEditingRoutes
import duesoldi.controller.RequestContextDirective._
import duesoldi.dependencies.{AppDependencies, AppDependenciesImpl}

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Controller
  with Configured
  with AppDependencies
  with RequestDependenciesDirective
  with AccessRecording
{

  implicit val appDependencies: AppDependenciesImpl = new AppDependenciesImpl(config)

  lazy val routes: Route =
    inContext { implicit requestContext: RequestContext =>
      withDependencies(requestContext) { requestDependencies =>
        recordAccess {
          furnitureRoutes(config) ~
          blogIndexRoutes(requestDependencies.indexPageMaker, requestDependencies.events) ~
          blogEntryRoutes(requestDependencies.makeEntryPage) ~
          metricsRoutes(config.adminCredentials, appDependencies.accessRecordStore) ~
          robotsRoutes ~
          blogEditingRoutes(config.adminCredentials, appDependencies.blogStore) ~
          debugRoutes(config.adminCredentials)
        }
      }
    }

}
