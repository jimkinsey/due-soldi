package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.Env
import duesoldi.config.Config.Credentials
import duesoldi.config.EnvironmentalConfig
import duesoldi.controller.AdminAuthentication.adminsOnly

object DebugRoutes {
  def debugRoutes(adminCredentials: Option[Credentials], env: Env): Route = pathPrefix("admin" / "debug") {
    adminsOnly(adminCredentials) { _ =>
      pathPrefix("headers") {
        extractRequest { req =>
          complete {
            req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
          }
        }
      } ~ pathPrefix("config") {
        complete {
          env
            .filterNot { case (key, _) => EnvironmentalConfig.sensitiveVars.contains(key) }
            .map { case (key, value) => s"$key=$value" } mkString "\n"
        }
      }
    }
  }
}
