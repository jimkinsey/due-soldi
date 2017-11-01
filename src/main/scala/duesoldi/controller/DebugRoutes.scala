package duesoldi.controller

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.config.Config.Credentials
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.controller.DebugRoutes.{MakeConfigPage, MakeHeadersPage}
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

object HeadersPageMaker
{
  def makeHeadersPage: MakeHeadersPage =
    _.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
}

object ConfigPageMaker
{
  def makeConfigPage(config: Config): MakeConfigPage = { () =>
    nonSensitive(toEnv(config)).map { case (key, value) => s"$key=$value" } mkString "\n"
  }
}

