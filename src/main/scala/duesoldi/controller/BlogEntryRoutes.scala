package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.controller.BlogEntryRoutes.Event
import duesoldi.controller.BlogEntryRoutes.Event.{BlogEntryPageRendered, EntryNotFound, InvalidId}
import duesoldi.events.Events
import duesoldi.page.EntryPageMaker
import duesoldi.page.EntryPageMaker.Failure

import scala.concurrent.ExecutionContext

trait BlogEntryRoutes {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  type EntryPage = (String) => EntryPageMaker.Result

  def entryPage: EntryPage
  def events: Events

  lazy val blogEntryRoutes = path("blog" / Segment) { entryId =>
    complete {
      for {
        page <- entryPage(entryId)
      } yield {
        page match {
          case Right(html) =>
            events.notify(BlogEntryPageRendered(html))
            HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
          case Left(Failure.EntryNotFound) =>
            events.notify(EntryNotFound(entryId))
            HttpResponse(NotFound)
          case Left(Failure.InvalidId) =>
            events.notify(InvalidId(entryId))
            HttpResponse(BadRequest)
          case Left(failure: Failure.RenderFailure) =>
            events.notify(Event.RenderFailure(failure))
            HttpResponse(InternalServerError)
        }
      }
    }
  }

}

object BlogEntryRoutes {
  sealed trait Event extends duesoldi.events.Event
  object Event {
    case class BlogEntryPageRendered(html: String) extends Event
    case class EntryNotFound(id: String) extends Event
    case class InvalidId(id: String) extends Event
    case class RenderFailure(cause: Failure.RenderFailure) extends Event
  }
}
