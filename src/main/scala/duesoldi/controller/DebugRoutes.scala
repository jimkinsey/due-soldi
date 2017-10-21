package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.Env
import duesoldi.config.EnvironmentalConfig

object DebugRoutes {
  def debugRoutes(env: Env): Route = path("admin" / "debug" / "headers") {
    extractRequest { req =>
      complete { req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n" }
    }
  } ~ path("admin" / "debug" / "config") {
    complete {
      env
        .filterNot { case (key, _) => EnvironmentalConfig.sensitiveVars.contains(key) }
        .map { case (key, value) => s"$key=$value" } mkString "\n"
    }
  }
}
