package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.controller.AdminAuthentication.adminsOnly

object DebugRoutes
{
  def debugRoutes(implicit requestContext: RequestContext): Route = pathPrefix("admin" / "debug") {
    adminsOnly(requestContext.config.adminCredentials) { _ =>
      pathPrefix("headers") {
        extractRequest { req =>
          complete {
            req.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
          }
        }
      } ~ pathPrefix("config") {
        complete {
          nonSensitive(toEnv(requestContext.config)).map { case (key, value) => s"$key=$value" } mkString "\n"
        }
      }
    }
  }
}
