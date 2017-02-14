package duesoldi.testapp

import duesoldi.{App, Env, Server}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class ServerStartFailure(attempt: Int) extends Exception

object TestApp {

  def start(env: Env)(implicit ec: ExecutionContext): Future[Server] = {
    def newServer(attempt: Int = 0): Future[Server] = attempt match {
      case n if n > maxStartupAttempts => Future.failed(ServerStartFailure(attempt))
      case _ =>
        App.startServer("localhost", newPort, env)().recoverWith {
          case _ => newServer(attempt + 1)
        }
    }

    newServer()
  }

  private def newPort = Random.nextInt(portRange.size) + portRange.start

  private lazy val portRange = 9000 to 9999
  private lazy val maxStartupAttempts = 5
}
