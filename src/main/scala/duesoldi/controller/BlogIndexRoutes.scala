package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.controller.BlogIndexRoutes.Event.{BlogIndexPageNotRendered, BlogIndexPageRendered}
import duesoldi.events.Events
import duesoldi.page.{IndexPageFailure, IndexPageMaker}

import scala.concurrent.ExecutionContext

object BlogIndexRoutes {
  def blogIndexRoutes(indexPageMaker: IndexPageMaker, events: Events)(implicit executionContext: ExecutionContext): Route = pathPrefix("blog") {
    pathEndOrSingleSlash {
      redirectToTrailingSlashIfMissing(MovedPermanently) {
        complete {
          for {
            page <- indexPageMaker.indexPage
          } yield {
            page match {
              case Right(html) =>
                events.emit(BlogIndexPageRendered(html))
                HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
              case Left(failure: IndexPageFailure.BlogStoreEmpty.type) =>
                events.emit(BlogIndexPageNotRendered(failure))
                HttpResponse(NotFound)
              case Left(failure) =>
                events.emit(BlogIndexPageNotRendered(failure))
                HttpResponse(InternalServerError)
            }
          }
        }
      }
    }
  }

  sealed trait Event extends duesoldi.events.Event
  object Event {
    case class BlogIndexPageRendered(html: String) extends Event
    case class BlogIndexPageNotRendered(reason: IndexPageFailure) extends Event
  }
}

