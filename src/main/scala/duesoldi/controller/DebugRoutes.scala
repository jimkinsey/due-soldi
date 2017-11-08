package duesoldi.controller

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector

object DebugRoutes
{
  type MakeHeadersPage = HttpRequest => String
  type MakeConfigPage = () => String

  def debugRoutes(implicit inject: RequestDependencyInjector): Route = pathPrefix("admin" / "debug") {
    inject.dependencies[Credentials,MakeHeadersPage,MakeConfigPage] into { case (credentials, makeHeadersPage, makeConfigPage) =>
      adminsOnly(credentials) {
        pathPrefix("headers") {
          extractRequest { req =>
            complete {
              makeHeadersPage(req)
            }
          }
        } ~ pathPrefix("config") {
          complete {
            makeConfigPage()
          }
        }
      }
    }
  }
}
