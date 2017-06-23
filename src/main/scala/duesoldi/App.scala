package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import duesoldi.controller.MasterController

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object App {
  def main(args: Array[String]) {
    val host = Option(System.getenv("HOST")).getOrElse("0.0.0.0")
    val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
    val env: Map[String, String] = System.getenv().asScala.toMap

    println(s"Binding to $host:$port")
    val s = startServer(host, port, env) {
      case Success(_) => println(s"Bound to $host:$port")
      case Failure(ex)      =>
        System.err.println(s"Failed to start server: ${ex.getMessage}")
        ex.printStackTrace(System.err)
    }
  }

  def startServer(host: String, port: Int, env: Env)(complete: Try[Server] => Unit = _ => ()): Future[Server] = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val controller = new MasterController(env)

    val fut = Http().bindAndHandle(controller.routes, host, port) map { binding => new Server(binding)}
    fut.onComplete(complete)
    fut
  }

}

trait Controller {
  def routes: Route
}

object Server {

  type Complete = Try[Server] => Unit

  val NoopComplete: Complete = _ => ()

  def startServer(controller: Controller, host: String, port: Int)(complete: Complete = NoopComplete): Future[Server] = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val fut = Http().bindAndHandle(controller.routes, host, port) map { binding => new Server(binding)}
    fut.onComplete(complete)
    fut
  }

}

class Server(binding: ServerBinding)(implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {
  lazy val port = binding.localAddress.getPort
  lazy val host = binding.localAddress.getHostName

  def stop(): Future[Unit] = {
    for {
      _ <- binding.unbind()
      _ <- actorSystem.terminate()
    } yield { }
  }
}