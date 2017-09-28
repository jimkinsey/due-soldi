package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.page.{IndexPageFailure, IndexPageMaker}

import scala.concurrent.ExecutionContext

trait BlogIndexRoutes extends AccessRecording {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  def indexPageMaker: IndexPageMaker

  lazy val blogIndexRoutes = pathPrefix("blog") {
    pathEndOrSingleSlash {
      redirectToTrailingSlashIfMissing(MovedPermanently) {
        recordAccess {
          complete {
            for {
              page <- indexPageMaker.indexPage
            } yield {
              page match {
                case Right(html) =>
                  HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
                case Left(IndexPageFailure.BlogStoreEmpty) =>
                  System.err.println(s"No blog entries in the store, not rendering index page")
                  HttpResponse(NotFound)
                case Left(failure) =>
                  System.err.println(s"Failed to render blog index page - $failure")
                  HttpResponse(InternalServerError)
              }
            }
          }
        }
      }
    }
  }

}

