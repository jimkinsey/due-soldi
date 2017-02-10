package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

case object InvalidId
case object BlogStoreEmpty

object App extends Routing {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val host = Option(System.getenv("HOST")).getOrElse("0.0.0.0")
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)

    implicit val env: Map[String, String] = System.getenv().asScala.toMap

    println(s"Binding to $host:$port")
    Http().bindAndHandle(routes, host, port) onComplete {
      case Success(binding) => println(s"Bound to ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
      case Failure(ex)      =>
        System.err.println(s"Failed to bind server: ${ex.getMessage}")
        ex.printStackTrace(System.err)
    }
  }
}