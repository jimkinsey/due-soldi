package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured

trait DebugRoutes extends AdminAuthentication { self: Configured =>

  lazy val debugRoutes = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      adminsOnly(config.adminCredentials) {
        complete { req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n" }
      }
    }
  }

}
