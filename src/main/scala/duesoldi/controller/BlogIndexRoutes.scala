package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.controller.BlogIndexRoutes.Event.{BlogIndexPageNotRendered, BlogIndexPageRendered}
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.events.Emit
import duesoldi.page.{IndexPageFailure, IndexPageMaker}

import scala.concurrent.ExecutionContext

object BlogIndexRoutes
{
  def blogIndexRoutes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector): Route =
    pathPrefix("blog") {
      pathEndOrSingleSlash {
        redirectToTrailingSlashIfMissing(MovedPermanently) {
          inject.dependencies[Emit, IndexPageMaker] into { case (emit, indexPageMaker) =>
            complete {
              for {
                page <- indexPageMaker.indexPage
              } yield {
                page match {
                  case Right(html) =>
                    emit(BlogIndexPageRendered(html))
                    HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
                  case Left(failure: IndexPageFailure.BlogStoreEmpty.type) =>
                    emit(BlogIndexPageNotRendered(failure))
                    HttpResponse(NotFound)
                  case Left(failure) =>
                    emit(BlogIndexPageNotRendered(failure))
                    HttpResponse(InternalServerError)
                }
              }
            }
          }
        }
      }
    }

  sealed trait Event
  object Event
  {
    case class BlogIndexPageRendered(html: String) extends Event
    case class BlogIndexPageNotRendered(reason: IndexPageFailure) extends Event
  }
}

