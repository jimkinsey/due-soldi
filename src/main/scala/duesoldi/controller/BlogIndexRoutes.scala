package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogIndexPageModel, Renderer}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

import scala.util.Try

case object BlogStoreEmpty

import scala.concurrent.{ExecutionContext, Future}

trait BlogIndexRoutes extends AccessRecording {
  self: Configured  =>

  import cats.instances.all._

  implicit def executionContext: ExecutionContext

  def blogStore: BlogStore
  def renderer: Renderer

  lazy val blogIndexRoutes = pathPrefix("blog") {
    pathEndOrSingleSlash {
      redirectToTrailingSlashIfMissing(MovedPermanently) {
        recordAccess {
          complete {
            (for {
              entries <- blogEntries
              model = BlogIndexPageModel(
                entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.flatMap {
                  case BlogEntry(id, content, lastModified) =>
                    Try(BlogIndexPageModel.Entry(
                      lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                      title = MarkdownDocument.title(content).get,
                      id = id
                    )).toOption
                },
                furnitureVersion = config.furnitureVersion
              )
              html <- EitherT(renderer.render("blog-index", model)).leftMap(_.asInstanceOf[Any])
            } yield {
              html
            }).value map {
              case Right(html) => HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
              case Left(BlogStoreEmpty) =>
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

  private def blogEntries = EitherT[Future, BlogStoreEmpty.type, Seq[BlogEntry]](blogStore.entries.map { entries =>
    entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
      case Nil   => Left(BlogStoreEmpty)
      case other => Right(other)
    }
  })

}
