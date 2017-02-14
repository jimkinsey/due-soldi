package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownToHtmlConverter
import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogEntryPageModel, BlogIndexPageModel, Renderer}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

case object InvalidId
case object BlogStoreEmpty

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
        model   = BlogIndexPageModel(
          entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.map { case BlogEntry(id, title, _, lastModified) => BlogIndexPageModel.Entry(
            lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
            title = title,
            id = id) },
          furnitureVersion = config.furnitureVersion
        )
        html    <- EitherT(renderer.render("blog-index", model)).leftMap(_.asInstanceOf[Any])
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
        model = BlogEntryPageModel(
          title = entry.title,
          lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
          contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes).mkString,
          furnitureVersion = config.furnitureVersion)
        html  <- EitherT(renderer.render("blog-entry", model)).leftMap(_.asInstanceOf[Any])
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
