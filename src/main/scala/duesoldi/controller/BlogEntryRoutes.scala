package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.page.EntryPageMaker
import duesoldi.page.EntryPageMaker.Failure

import scala.concurrent.ExecutionContext

trait BlogEntryRoutes {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  type EntryPage = (String) => EntryPageMaker.Result

  def entryPage: EntryPage

  lazy val blogEntryRoutes = path("blog" / Segment) { entryId =>
    complete {
      entryPage(entryId) map {
        case Right(html) =>
          HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
        case Left(Failure.EntryNotFound(id)) =>
          HttpResponse(NotFound)
        case Left(Failure.InvalidId(id)) =>
          HttpResponse(BadRequest)
        case Left(failure: Failure.RenderFailure) =>
          HttpResponse(InternalServerError)
      }
    }
  }
}
