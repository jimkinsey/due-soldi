package duesoldi.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import duesoldi.config.Configured
import duesoldi.storage.BlogStore

import scala.concurrent.ExecutionContext

trait BlogEditingRoutes extends AdminAuthentication { self: Configured =>
  implicit def executionContext: ExecutionContext
  implicit def materializer: Materializer

  def blogStore: BlogStore

  final def blogEditingRoutes = path("admin" / "blog" / Remaining) { remaining =>
    put {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        entity(as[String]) { content =>
          complete {
            for {
              result <- blogStore.createOrUpdate(remaining, content)
            } yield {
              result match {
                case BlogStore.Created => HttpResponse(201)
                case _ => ???
              }
            }
          }
        }
      }
    }
  }

}
