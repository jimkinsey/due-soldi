package duesoldi.controller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{Referer, `User-Agent`}
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownToHtmlConverter
import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogEntryPageModel, BlogIndexPageModel, Renderer}
import duesoldi.storage.AccessRecordStore.Access
import duesoldi.storage.{AccessRecordStore, BlogStore}
import duesoldi.validation.ValidIdentifier

import scala.util.Failure

case object InvalidId
case object BlogStoreEmpty

import scala.concurrent.{ExecutionContext, Future}

trait BlogRoutes { self: Configured =>
  import cats.instances.all._

  implicit def executionContext: ExecutionContext

  def blogStore: BlogStore
  def renderer: Renderer
  def accessRecordStore: AccessRecordStore

  final def blogRoutes = path("blog" / ) {
    recordAccess {
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
          case Left(BlogStoreEmpty)     =>
            System.err.println(s"No blog entries in the store, not rendering index page")
            HttpResponse(NotFound)
          case Left(failure)            =>
            System.err.println(s"Failed to render blog index page - $failure")
            HttpResponse(InternalServerError)
        }
      }
    }
  } ~ pathPrefix("blog" / Remaining) { remaining =>
    recordAccess {
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
          case Left(BlogStore.NotFound) =>
            System.err.println(s"Blog $remaining not found")
            HttpResponse(NotFound)
          case Left(InvalidId)          =>
            System.err.println(s"ID '$remaining' is invalid")
            HttpResponse(BadRequest)
          case Left(failure)            =>
            System.err.println(s"Failed to render blog '$remaining' - $failure")
            HttpResponse(InternalServerError)
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

  private def recordAccess = mapRequest { req =>
    if (config.accessRecordingEnabled) {
      println(s"Request headers: ${req.headers}")
      accessRecordStore.record(Access(
        time = ZonedDateTime.now(),
        path = req.uri.path.toString,
        referer = req.header[Referer].map(_.getUri().toString),
        userAgent = req.header[`User-Agent` ].map(_.value())
      )).onComplete {
        case Failure(ex) =>
          System.err.println(s"Failed to record access - ${ex.getMessage}")
          ex.printStackTrace()
        case _ =>
      }
    }
    req
  }

}
