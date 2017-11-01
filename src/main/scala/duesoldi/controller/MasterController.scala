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
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.controller.TaggedRequestDirective._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector

import scala.concurrent.ExecutionContext

object MasterController
{
  def routes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector, appConfig: Config): Route = {
    tagRequest {
      recordAccess(inject) {
        robotsRoutes ~
        furnitureRoutes ~
        blogIndexRoutes ~
        blogEntryRoutes ~
        blogEditingRoutes ~
        metricsRoutes ~
        debugRoutes ~
        learnJapaneseRoutes
      }
    }
  }
}
