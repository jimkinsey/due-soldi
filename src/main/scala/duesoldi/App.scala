package duesoldi

import java.io.{File, FileNotFoundException}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable._
import akka.http.scaladsl.model.HttpCharsets.`UTF-8`
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait Routing {
  val routes =
    path("ping") {
      get {
        complete { "pong" }
      }
    } ~ path("pong") {
      get {
        complete { "ping" }
      }
    } ~ pathPrefix("blog" / Remaining) { name =>
      complete {
        Try(Source.fromFile(new File(s"/tmp/blog/$name.md")).mkString) match {
          case Success(content) =>
            HttpResponse(OK, entity = HttpEntity(ContentType(`text/html`, `UTF-8`), content))
          case Failure(exception: FileNotFoundException) =>
            HttpResponse(NotFound)
          case Failure(exception) =>
            println(exception)
            HttpResponse(InternalServerError)
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

    println(s"Binding to $host:$port")
    Http().bindAndHandle(routes, host, port) onComplete {
      case Success(binding) => println(s"Bound to ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
      case Failure(ex)      =>
        System.err.println(s"Failed to bind server: ${ex.getMessage}")
        ex.printStackTrace(System.err)
    }
  }
}