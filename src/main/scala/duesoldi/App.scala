package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}

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
    }
}

object App extends Routing {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val (host, port) = "localhost" -> Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

    println(s"Binding to $host:$port")
    Http().bindAndHandle(routes, host, port) onComplete {
      case Success(binding) => println(s"Bound to ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
      case Failure(ex)      =>
        System.err.println(s"Failed to bind server")
        ex.printStackTrace(System.err)
    }
  }
}