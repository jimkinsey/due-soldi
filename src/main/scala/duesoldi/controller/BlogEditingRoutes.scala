package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import duesoldi.config.Configured
import duesoldi.storage.BlogStore
import duesoldi.storage.BlogStore.{Created, Invalid}

import scala.concurrent.ExecutionContext

trait BlogEditingRoutes extends AdminAuthentication { self: Configured =>
  implicit def executionContext: ExecutionContext
  implicit def materializer: Materializer

  def blogStore: BlogStore

  final def blogEditingRoutes = path("admin" / "blog" / Remaining) { remaining =>
    put {
      adminsOnly {
        entity(as[String]) { content =>
          complete {
            for {
              result <- blogStore.store(remaining, ZonedDateTime.now(), content)
            } yield {
              result match {
                case Created(_)       => HttpResponse(201)
                case Invalid(reasons) => HttpResponse(400, entity = reasons.mkString("\n"))
              }
            }
          }
        }
      }
    } ~ delete {
      adminsOnly {
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
