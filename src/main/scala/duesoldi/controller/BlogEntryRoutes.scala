package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.config.Configured
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.rendering.{BlogEntryPageModel, Renderer}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

trait BlogEntryRoutes extends AccessRecording {
  self: Configured =>

  implicit def executionContext: ExecutionContext

  import cats.instances.all._

  def blogStore: BlogStore
  def renderer: Renderer

  case object InvalidId
  case object EntryNotFound

  lazy val blogEntryRoutes = path("blog" / Segment) { entryId =>
    recordAccess {
      complete {
        (for {
          name  <- EitherT.fromOption[Future](ValidIdentifier(entryId), { InvalidId })
          entry <- EitherT(blogStore.entry(name).map { _.toRight({ EntryNotFound }) })
          model = BlogEntryPageModel(
            title = MarkdownDocument.title(entry.content).get,
            lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
            contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes).mkString,
            furnitureVersion = config.furnitureVersion)
          html  <- EitherT(renderer.render("blog-entry", model)).leftMap(_.asInstanceOf[Any])
        } yield {
          html
        }).value map {
          case Right(html)              => HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
          case Left(EntryNotFound)      =>
            System.err.println(s"Blog $entryId not found")
            HttpResponse(NotFound)
          case Left(InvalidId)          =>
            System.err.println(s"ID '$entryId' is invalid")
            HttpResponse(BadRequest)
          case Left(failure)            =>
            System.err.println(s"Failed to render blog '$entryId' - $failure")
            HttpResponse(InternalServerError)
        }
      }
    }
  }

}
