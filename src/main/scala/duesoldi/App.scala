package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable._
import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound, OK}
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import cats.data.EitherT
import duesoldi.markdown.MarkdownParser
import duesoldi.model.BlogEntry
import duesoldi.rendering.Renderer
import duesoldi.storage.{BlogStore, FilesystemMarkdownSource}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case object InvalidId
case object BlogStoreEmpty

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

object App extends Routing {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val host = Option(System.getenv("HOST")).getOrElse("0.0.0.0")
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

    implicit val blogSourceConfig: FilesystemMarkdownSource.Config = new FilesystemMarkdownSource.Config {
      override def path: String = System.getenv("BLOG_STORE_PATH")
    }

    println(s"Binding to $host:$port")
    Http().bindAndHandle(routes, host, port) onComplete {
      case Success(binding) => println(s"Bound to ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
      case Failure(ex)      =>
        System.err.println(s"Failed to bind server: ${ex.getMessage}")
        ex.printStackTrace(System.err)
    }
  }
}