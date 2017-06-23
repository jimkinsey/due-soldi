package duesoldi.controller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Referer, `User-Agent`}
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.assets.AssetStore
import duesoldi.config.Configured
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogEntryPageModel, BlogIndexPageModel, Renderer}
import duesoldi.storage.AccessRecordStore.Access
import duesoldi.storage.{AccessRecordStore, BlogStore}
import duesoldi.validation.ValidIdentifier

import scala.util.{Failure, Try}

case object InvalidId
case object BlogStoreEmpty
case object EntryNotFound // FIXME name

import scala.concurrent.{ExecutionContext, Future}

trait BlogRoutes extends AccessRecording {
  self: Configured  =>

  import cats.instances.all._

  implicit def executionContext: ExecutionContext

  def blogStore: BlogStore
  def assetStore: AssetStore
  def renderer: Renderer

  final def blogRoutes = pathPrefix("blog") {
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
  } ~ path("blog" / Segment / Remaining) { case (entryId, assetPath) =>
    extractUri { uri =>
      complete {
        (for {
          name  <- EitherT.fromOption[Future](ValidIdentifier(entryId), { InvalidId })
          entry <- EitherT(blogStore.entry(name).map { _.toRight({ EntryNotFound }) })
          asset <- EitherT(assetStore.asset(uri.path.toString))
          contentType <- EitherT.fromEither[Future](ContentType.parse(asset.contentType)).leftMap(_.asInstanceOf[Any])
        } yield {
          (asset.data, contentType)
        }).value map {
          case Right((data: Array[Byte], contentType: ContentType)) => HttpResponse(entity = HttpEntity(contentType, data))
          case Left(InvalidId) => HttpResponse(status = BadRequest)
          case Left(EntryNotFound) => HttpResponse(status = NotFound)
          case Left(AssetStore.AssetNotFound) => HttpResponse(status = NotFound)
          case Left(AssetStore.UpstreamError) => HttpResponse(status = BadGateway)
          case Left(_) => HttpResponse(status = InternalServerError)
        }
      }
    }
  } ~ path("blog" / Segment) { entryId =>
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

  private def blogEntries = EitherT[Future, BlogStoreEmpty.type, Seq[BlogEntry]](blogStore.entries.map { entries =>
    entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
      case Nil   => Left(BlogStoreEmpty)
      case other => Right(other)
    }
  })

}
