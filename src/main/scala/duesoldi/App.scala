package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import duesoldi.config.EnvironmentalConfig
import duesoldi.controller.MasterController
import duesoldi.dependencies.AppDependencies
import duesoldi.logging.Logger

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object App {
  def main(args: Array[String]) {
    val env: Map[String, String] = System.getenv().asScala.toMap
    start(env)
  }

  def start(env: Env) = {
    val config = EnvironmentalConfig(env)
    lazy val logger = new Logger("Application", config.loggingEnabled)
    implicit val executionContext = concurrent.ExecutionContext.Implicits.global
    implicit val appDependencies = new AppDependencies(config)
    Server.startServer(new MasterController(config), config.host, config.port) {
      case Success(server) =>
        logger.info(s"Started server on ${server.host}:${server.port}")
      case Failure(ex) =>
        logger.error(s"Failed to start server on ${config.host}:${config.port} - ${ex.getMessage}")
    }
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