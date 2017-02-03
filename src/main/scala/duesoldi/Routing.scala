package duesoldi

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import duesoldi.markdown.MarkdownParser
import duesoldi.model.BlogEntry
import duesoldi.rendering.Renderer
import duesoldi.storage.{BlogStore, FilesystemMarkdownSource}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

trait Routing {
  import cats.instances.all._

  def routes(implicit executionContext: ExecutionContext, blogSourceConfig: FilesystemMarkdownSource.Config) = {
    val store = new BlogStore(new FilesystemMarkdownSource, new MarkdownParser)
    val renderer = new Renderer

    def blogEntries = EitherT[Future, BlogStoreEmpty.type, Seq[BlogEntry]](store.entries.map { entries =>
      entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
        case Nil   => Left(BlogStoreEmpty)
        case other => Right(other)
      }
    })

    path("blog" / ) {
      complete {
        (for {
          entries <- blogEntries
          html    <- EitherT(renderer.render(entries)).leftMap(_.asInstanceOf[Any])
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
          entry <- EitherT(store.entry(name))
          html  <- EitherT(renderer.render(entry)).leftMap(_.asInstanceOf[Any])
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
  }
}
