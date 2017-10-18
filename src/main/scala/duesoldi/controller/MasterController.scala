package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi._
import duesoldi.config.Config
import duesoldi.controller.BlogEditingRoutes.blogEditingRoutes
import duesoldi.controller.BlogEntryRoutes.blogEntryRoutes
import duesoldi.controller.BlogIndexRoutes.blogIndexRoutes
import duesoldi.controller.DebugRoutes.debugRoutes
import duesoldi.controller.FurnitureRoutes.furnitureRoutes
import duesoldi.controller.MetricsRoutes.metricsRoutes
import duesoldi.controller.RequestContextDirective._
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.dependencies.AppDependencies

import scala.concurrent.ExecutionContext

class MasterController(config: Config)
                      (implicit val executionContext: ExecutionContext, val appDependencies: AppDependencies)
  extends Controller
  with AccessRecording
  with RequestDependenciesDirective
{
  lazy val routes: Route =
    inContext { implicit requestContext: RequestContext =>
      withDependencies(requestContext) { requestDependencies =>
        recordAccess(appDependencies.accessRecordStore, appDependencies.events, config.accessRecordingEnabled) {
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
