package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import duesoldi.config.EnvironmentalConfig
import duesoldi.controller.MasterController
import duesoldi.logging.Logger

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object App {
  implicit val executionContext: ExecutionContext = concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    val env: Map[String, String] = System.getenv().asScala.toMap
    start(env)
  }

  def start(env: Env): Future[Server] = {
    val config = EnvironmentalConfig(env)
    val logger = new Logger("App", config.loggingEnabled)
    val eventualServer = Server.start(MasterController.routes(config), config.host, config.port)
    eventualServer.onComplete {
      case Success(server) =>
        logger.info(s"Started server on ${server.host}:${server.port}")
      case Failure(ex) =>
        logger.error(s"Failed to start server on ${config.host}:${config.port} - ${ex.getMessage}")
    }
    eventualServer
  }
}

object Server {
  def start(route: Route, host: String, port: Int): Future[Server] = {
    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    Http().bindAndHandle(route, host, port) map { binding => new Server(binding) }
  }
}

class Server(binding: ServerBinding)(implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {
  lazy val port: Int = binding.localAddress.getPort
  lazy val host: String = binding.localAddress.getHostName

  def stop(): Future[Unit] = {
    for {
      _ <- binding.unbind()
      _ <- actorSystem.terminate()
    } yield { }
  }
}