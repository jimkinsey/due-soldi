package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.controller.AccessRecordingDirective.recordAccess
import duesoldi.controller.BlogEditingRoutes.blogEditingRoutes
import duesoldi.controller.BlogEntryRoutes.blogEntryRoutes
import duesoldi.controller.BlogIndexRoutes.blogIndexRoutes
import duesoldi.controller.DebugRoutes.debugRoutes
import duesoldi.controller.FurnitureRoutes.furnitureRoutes
import duesoldi.controller.MetricsRoutes.metricsRoutes
import duesoldi.controller.RequestContextDirective._
import duesoldi.controller.RequestDependenciesDirective.withDependencies
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.dependencies.AppDependencies

import scala.concurrent.ExecutionContext
import AdminAuthentication.adminsOnly
object MasterController
{
  def routes(config: Config)(implicit executionContext: ExecutionContext, appDependencies: AppDependencies): Route =
    inContext { implicit requestContext: RequestContext =>
      withDependencies { dependencies =>
        recordAccess(dependencies.accessRecordStore, dependencies.events, config.accessRecordingEnabled) {
          robotsRoutes ~
          furnitureRoutes(config) ~
          blogIndexRoutes(dependencies.indexPageMaker, dependencies.events) ~
          blogEntryRoutes(dependencies.makeEntryPage) ~
          adminsOnly(config.adminCredentials) { _ =>
            metricsRoutes(dependencies.accessRecordStore) ~
            blogEditingRoutes(dependencies.blogStore) ~
            debugRoutes
          }
        }
      }
    }
}
