package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.controller.BlogEntryRoutes.Event
import duesoldi.controller.BlogEntryRoutes.Event.{BlogEntryPageRendered, EntryNotFound, InvalidId, RenderFailure}
import duesoldi.events.Events
import duesoldi.page.{EntryPageFailure, EntryPageMaker}

import scala.concurrent.ExecutionContext

trait BlogEntryRoutes extends AccessRecording {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  def entryPageMaker: EntryPageMaker
  def events: Events

  lazy val blogEntryRoutes = path("blog" / Segment) { entryId =>
    recordAccess {
      complete {
        for {
          page <- entryPageMaker.entryPage(entryId)
        } yield {
          page match {
            case Right(html) =>
              events.notify(BlogEntryPageRendered(html))
              HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
            case Left(EntryPageFailure.EntryNotFound) =>
              events.notify(EntryNotFound(entryId))
              HttpResponse(NotFound)
            case Left(EntryPageFailure.InvalidId) =>
              events.notify(InvalidId(entryId))
              HttpResponse(BadRequest)
            case Left(failure: EntryPageFailure.RenderFailure) =>
              events.notify(Event.RenderFailure(failure))
              HttpResponse(InternalServerError)
          }
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
    case class RenderFailure(cause: EntryPageFailure.RenderFailure) extends Event
  }
}
