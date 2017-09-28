package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.page.{EntryPageFailure, EntryPageMaker}

import scala.concurrent.ExecutionContext

trait BlogEntryRoutes extends AccessRecording {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  def entryPageMaker: EntryPageMaker

  lazy val blogEntryRoutes = path("blog" / Segment) { entryId =>
    recordAccess {
      complete {
        for {
          page <- entryPageMaker.entryPage(entryId)
        } yield {
          page match {
            case Right(html) =>
              HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
            case Left(EntryPageFailure.EntryNotFound) =>
              System.err.println(s"Blog $entryId not found")
              HttpResponse(NotFound)
            case Left(EntryPageFailure.InvalidId) =>
              System.err.println(s"ID '$entryId' is invalid")
              HttpResponse(BadRequest)
            case Left(failure) =>
              System.err.println(s"Failed to render blog '$entryId' - $failure")
              HttpResponse(InternalServerError)
          }
        }
      }
    }
  }

}
