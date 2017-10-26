package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.config.{Config, EnvironmentalConfig}
import duesoldi.controller.AdminAuthentication.adminsOnly

object DebugRoutes {
  def debugRoutes(adminCredentials: Option[Credentials], config: Config): Route = pathPrefix("admin" / "debug") {
    adminsOnly(adminCredentials) { _ =>
      pathPrefix("headers") {
        extractRequest { req =>
          complete {
            req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
          }
        }
      } ~ pathPrefix("config") {
        complete {
          nonSensitive(toEnv(config)).map { case (key, value) => s"$key=$value" } mkString "\n"
        }
      }
    }
  }
}
