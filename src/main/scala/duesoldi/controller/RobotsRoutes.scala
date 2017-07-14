package duesoldi.controller

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

trait RobotsRoutes {
  implicit def executionContext: ExecutionContext

  lazy val robotsRoutes =
    path("robots.txt") {
      respondWithHeader(RawHeader("Cache-Control", "max-age=86400")) {
        complete { "User-agent: *\nDisallow:\n" }
      }
    }

}
