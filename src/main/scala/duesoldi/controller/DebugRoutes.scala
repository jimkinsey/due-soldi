package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured

trait DebugRoutes extends AdminAuthentication { self: Configured =>

  final def debugRoutes = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      adminsOnly {
        complete { req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n" }
      }
    }
  }

}
