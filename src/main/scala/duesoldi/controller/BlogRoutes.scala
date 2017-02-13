package duesoldi.controller

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.config.Configured
import duesoldi.model.BlogEntry
import duesoldi.rendering.Renderer
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier
import duesoldi.{BlogStoreEmpty, InvalidId}

import scala.concurrent.{ExecutionContext, Future}

trait BlogRoutes { self: Configured =>
  import cats.instances.all._

  implicit def executionContext: ExecutionContext

  def blogStore: BlogStore
  def renderer: Renderer

  final def blogRoutes = path("blog" / ) {
    complete {
      (for {
        entries <- blogEntries
        html    <- EitherT(renderer.render(entries, config.furnitureVersion)).leftMap(_.asInstanceOf[Any])
      } yield {
        html
      }).value map {
        case Right(html)              => HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
        case Left(BlogStoreEmpty)     => HttpResponse(NotFound)
        case Left(failure)            =>
          System.err.println(failure)
          HttpResponse(InternalServerError)
      }
    }
  } ~ pathPrefix("blog" / Remaining) { remaining =>
    complete {
      (for {
        name  <- EitherT.fromOption[Future](ValidIdentifier(remaining), { InvalidId })
        entry <- EitherT(blogStore.entry(name))
        html  <- EitherT(renderer.render(entry, config.furnitureVersion)).leftMap(_.asInstanceOf[Any])
      } yield {
        html
      }).value map {
        case Right(html)              => HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
        case Left(BlogStore.NotFound) => HttpResponse(NotFound)
        case Left(InvalidId)          => HttpResponse(BadRequest)
        case Left(failure)            =>
          System.err.println(failure)
          HttpResponse(InternalServerError)
      }
    }
  }

  private def blogEntries = EitherT[Future, BlogStoreEmpty.type, Seq[BlogEntry]](blogStore.entries.map { entries =>
    entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
      case Nil   => Left(BlogStoreEmpty)
      case other => Right(other)
    }
  })

}
