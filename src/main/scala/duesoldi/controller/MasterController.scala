package duesoldi.controller

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import duesoldi._
import duesoldi.config.Configured
import duesoldi.controller.RequestIdDirective._
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
    forRequestId { implicit requestId =>
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

}

case class RequestId(value: String)

object RequestIdDirective {
  def forRequestId: Directive1[RequestId] = provide(RequestId(UUID.randomUUID().toString))
}