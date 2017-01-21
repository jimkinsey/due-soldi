package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable._
import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import duesoldi.markdown.MarkdownParser
import duesoldi.rendering.Renderer
import duesoldi.storage.{BlogStore, FilesystemMarkdownSource}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait Routing {

  def routes(implicit executionContext: ExecutionContext, blogSourceConfig: FilesystemMarkdownSource.Config) = {
    val store = new BlogStore(new FilesystemMarkdownSource, new MarkdownParser)
    val renderer = new Renderer

    path("ping") {
      get {
        complete {
          "pong"
        }
      }
    } ~ path("pong") {
      get {
        complete {
          "ping"
        }
      }
    } ~ pathPrefix("blog" / Remaining) { name =>
      complete {
        store.entry(name).flatMap {
          case Left(f) => Future successful Left(f)
          case Right(entry) => renderer.render(entry)
        } map {
          case Left(BlogStore.NotFound) => HttpResponse(NotFound)
          case Left(failure) =>
            System.err.println(failure);
            HttpResponse(InternalServerError)
          case Right(html) => HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), html))
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