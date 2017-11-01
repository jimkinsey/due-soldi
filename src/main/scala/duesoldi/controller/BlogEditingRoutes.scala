package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.storage.BlogStore
import duesoldi.storage.BlogStore.{Created, Invalid}

import scala.concurrent.ExecutionContext

object BlogEditingRoutes
{
  def blogEditingRoutes(implicit executionContext: ExecutionContext,
                        inject: RequestDependencyInjector,
                        config: Config): Route =
    path("admin" / "blog" / Remaining) { remaining =>
      adminsOnly(config.adminCredentials) {
        inject.dependency[BlogStore] into { blogStore =>
          put {
            entity(as[String]) { content =>
              complete {
                for {
                  result <- blogStore.store(remaining, ZonedDateTime.now(), content)
                } yield {
                  result match {
                    case Created(_) => HttpResponse(201)
                    case Invalid(reasons) => HttpResponse(400, entity = reasons.mkString("\n"))
                  }
                }
              }
            }
          } ~ delete {
            complete {
              for {
                result <- blogStore.delete(remaining)
              } yield {
                HttpResponse(204)
              }
            }
          } ~ get {
            complete {
              for {
                result <- blogStore.entry(remaining)
              } yield {
                result match {
                  case Some(entry) => HttpResponse(200, entity = entry.content.raw)
                  case _ => HttpResponse(404)
                }
              }
            }
          }
        }
      }
    }
}
