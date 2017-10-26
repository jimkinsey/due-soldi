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
import duesoldi.controller.LearnJapaneseRoutes.learnJapaneseRoutes
import duesoldi.controller.MetricsRoutes.metricsRoutes
import duesoldi.controller.RequestContextDirective._
import duesoldi.controller.RequestDependenciesDirective.withDependencies
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.controller.WithConfigDirective.withConfig

import scala.concurrent.ExecutionContext

object MasterController
{
  def routes(appConfig: Config)(implicit executionContext: ExecutionContext): Route =
    inContext { implicit context: RequestContext =>
      withConfig(appConfig) { reqConfig =>
        withDependencies(reqConfig) { dependencies =>
          recordAccess(dependencies.accessRecordStore, dependencies.events, reqConfig.accessRecordingEnabled) {
            robotsRoutes ~
            furnitureRoutes(reqConfig) ~
            blogIndexRoutes(dependencies.indexPageMaker, dependencies.events) ~
            blogEntryRoutes(dependencies.makeEntryPage) ~
            blogEditingRoutes(appConfig.adminCredentials, dependencies.blogStore) ~
            metricsRoutes(appConfig.adminCredentials, dependencies.accessRecordStore) ~
            debugRoutes(appConfig.adminCredentials, reqConfig) ~
            learnJapaneseRoutes
          }
        }
      }
    }
}

