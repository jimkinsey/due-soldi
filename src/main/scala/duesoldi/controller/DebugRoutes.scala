package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object DebugRoutes {
  def debugRoutes: Route = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      complete { req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n" }
    }
  }
}
