package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi._
import duesoldi.config.Configured
import duesoldi.dependencies.{AppDependencies, RequestDependencies}

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Controller
  with Configured
  with AppDependencies
  with RequestDependencies
  with AccessRecording
  with FurnitureRoutes
  with MetricsRoutes
  with BlogIndexRoutes
  with BlogEntryRoutes
  with RobotsRoutes
  with BlogEditingRoutes
  with DebugRoutes {

  lazy val routes: Route =
    recordAccess {
      furnitureRoutes ~
      blogIndexRoutes ~
      blogEntryRoutes ~
      metricsRoutes ~
      robotsRoutes ~
      blogEditingRoutes ~
      debugRoutes
    }

}
