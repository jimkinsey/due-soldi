package duesoldi.controller

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object RobotsRoutes {
  lazy val robotsRoutes: Route =
    path("robots.txt") {
      respondWithHeader(RawHeader("Cache-Control", "max-age=86400")) {
        complete { "User-agent: *\nDisallow:\n" }
      }
    }
}
