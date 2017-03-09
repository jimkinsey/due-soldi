package duesoldi.controller

import java.time.ZonedDateTime

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
    // TODO validate ID, content, etc. - or should this be a function of the store?
    put {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        entity(as[String]) { content =>
          complete {
            for {
              result <- blogStore.store(remaining, ZonedDateTime.now(), content)
            } yield {
              HttpResponse(201)
            }
          }
        }
      }
    } ~ delete {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        entity(as[String]) { content =>
          complete {
            for {
              result <- blogStore.delete(remaining)
            } yield {
              HttpResponse(204)
            }
          }
        }
      }
    }

  }

}
