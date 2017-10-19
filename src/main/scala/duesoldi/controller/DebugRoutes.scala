package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import AdminAuthentication.adminsOnly

object DebugRoutes {
  def debugRoutes(credentials: Option[Credentials]): Route = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      adminsOnly(credentials) {
        complete { req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n" }
      }
    }
  }
}
