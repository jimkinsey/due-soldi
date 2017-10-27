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
import duesoldi.controller.RobotsRoutes.robotsRoutes

import scala.concurrent.ExecutionContext

object MasterController
{
  def routes(appConfig: Config)(implicit executionContext: ExecutionContext): Route =
    inContext(appConfig) { implicit context: RequestContext =>
      recordAccess(context) {
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
