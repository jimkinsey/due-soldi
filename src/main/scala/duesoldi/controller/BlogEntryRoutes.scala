package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.dependencies.RequestDependencies
import duesoldi.page.EntryPageMaker
import duesoldi.page.EntryPageMaker.Failure

import scala.concurrent.ExecutionContext

object BlogEntryRoutes {
  type MakeEntryPage = (String) => EntryPageMaker.Result
}

trait BlogEntryRoutes { self: RequestDependencies =>
  implicit def executionContext: ExecutionContext

  lazy val blogEntryRoutes: Route = path("blog" / Segment) { entryId =>
    extractRequest { implicit request =>
      withDependency[MakeEntryPage] { entryPage =>
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
  }
}
