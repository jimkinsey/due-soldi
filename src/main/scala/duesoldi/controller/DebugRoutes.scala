package duesoldi.controller

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured

trait DebugRoutes extends AdminAuthentication { self: Configured =>

  final def debugRoutes = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      adminsOnly {
        complete { req.headers.map { case HttpHeader(key, value) => s"$key: $value" } mkString "\n" }
      }
    }
  }

}
