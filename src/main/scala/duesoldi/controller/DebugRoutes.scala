package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.controller.RequestConfigDirective.requestConfig

object DebugRoutes
{
  def debugRoutes(implicit config: Config): Route = pathPrefix("admin" / "debug") {
    adminsOnly(config.adminCredentials) { _ =>
      pathPrefix("headers") {
        extractRequest { req =>
          complete {
            req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
          }
        }
      } ~ pathPrefix("config") {
        requestConfig(config) { reqConfig =>
          complete {
            nonSensitive(toEnv(reqConfig)).map { case (key, value) => s"$key=$value" } mkString "\n"
          }
        }
      }
    }
  }
}
