package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.storage.BlogStore
import duesoldi.storage.BlogStore.{Created, Invalid}

import scala.concurrent.ExecutionContext

trait BlogEditingRoutes extends AdminAuthentication { self: Configured =>
  implicit def executionContext: ExecutionContext

  def blogStore: BlogStore

  lazy val blogEditingRoutes = path("admin" / "blog" / Remaining) { remaining =>
    adminsOnly {
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
