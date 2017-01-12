package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

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

    val bindingFuture = Http().bindAndHandle(routes, host, port)

    // The following makes it easier to work with SBT, adapted from the official Akka docs
    bindingFuture.map { server =>
      println(s"Server online at http://${server.localAddress.getHostName}:${server.localAddress.getPort}/\nPress RETURN to stop...")
    }
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}