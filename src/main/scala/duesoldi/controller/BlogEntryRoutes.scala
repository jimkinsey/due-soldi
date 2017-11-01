package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.page.EntryPageMaker
import duesoldi.page.EntryPageMaker.Failure

import scala.concurrent.ExecutionContext

object BlogEntryRoutes
{
  type MakeEntryPage = (String) => EntryPageMaker.Result

  def blogEntryRoutes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector): Route =
    path("blog" / Segment) { entryId =>
      inject.dependency[MakeEntryPage] into { makeEntryPage =>
        complete {
          makeEntryPage(entryId) map {
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
}
