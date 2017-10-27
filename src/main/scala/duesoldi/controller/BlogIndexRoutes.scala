package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.controller.BlogIndexRoutes.Event.{BlogIndexPageNotRendered, BlogIndexPageRendered}
import duesoldi.controller.RequestDependenciesDirective.withDependencies
import duesoldi.events.Events
import duesoldi.page.{IndexPageFailure, IndexPageMaker}

import scala.concurrent.ExecutionContext

object BlogIndexRoutes
{
  def blogIndexRoutes(implicit executionContext: ExecutionContext, config: Config): Route =
    pathPrefix("blog") {
      pathEndOrSingleSlash {
        redirectToTrailingSlashIfMissing(MovedPermanently) {
          withDependencies(config) { dependencies =>
            complete {
              val indexPageMaker: IndexPageMaker = dependencies.indexPageMaker
              val events: Events = dependencies.events
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
    }

  sealed trait Event extends duesoldi.events.Event
  object Event
  {
    case class BlogIndexPageRendered(html: String) extends Event
    case class BlogIndexPageNotRendered(reason: IndexPageFailure) extends Event
  }
}

