package duesoldi.testapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import duesoldi.Routing
import duesoldi.storage.FilesystemMarkdownSource

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait TestServer {
  def port: Int
}
case class AkkaHttpTestServer(serverBinding: ServerBinding) extends TestServer {
  val port = serverBinding.localAddress.getPort
}

case class ServerStartFailure(attempt: Int) extends Exception

object TestApp extends Routing {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  def start(implicit ec: ExecutionContext, config: FilesystemMarkdownSource.Config): Future[TestServer] = {
    def newServer(attempt: Int = 0): Future[ServerBinding] = attempt match {
      case n if n > maxStartupAttempts => Future.failed(ServerStartFailure(attempt))
      case _ =>
        Http().bindAndHandle(routes, "localhost", newPort).recoverWith {
          case _ => newServer(attempt + 1)
        }
    }

    newServer().map(AkkaHttpTestServer)
  }

  def stop(server: TestServer)(implicit ec: ExecutionContext): Future[Unit] = server match {
    case AkkaHttpTestServer(binding) => binding.unbind()
  }

  private def newPort = Random.nextInt(portRange.size) + portRange.start

  private lazy val portRange = 9000 to 9999
  private lazy val maxStartupAttempts = 5

}
