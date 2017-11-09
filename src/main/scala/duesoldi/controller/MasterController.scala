package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.metrics.routes.AccessRecordingDirective.recordAccess
import duesoldi.blog.routes.BlogEditingRoutes.blogEditingRoutes
import duesoldi.blog.routes.BlogEntryRoutes.blogEntryRoutes
import duesoldi.blog.routes.BlogIndexRoutes.blogIndexRoutes
import duesoldi.controller.DebugRoutes.debugRoutes
import duesoldi.controller.FurnitureRoutes.furnitureRoutes
import duesoldi.controller.LearnJapaneseRoutes.learnJapaneseRoutes
import duesoldi.metrics.routes.MetricsRoutes.metricsRoutes
import duesoldi.controller.RobotsRoutes.robotsRoutes
import duesoldi.controller.TaggedRequestDirective._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector

import scala.concurrent.ExecutionContext

object MasterController
{
  def routes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector): Route = {
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
