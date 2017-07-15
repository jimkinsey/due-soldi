package duesoldi.testapp

import duesoldi.{App, Controller, Env, Server}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class ServerStartFailure(attempt: Int) extends Exception

object TestApp {

  def start(env: Env)(implicit ec: ExecutionContext): Future[Server] = {
    def newServer(attempt: Int = 0): Future[Server] = attempt match {
      case n if n > maxStartupAttempts => Future.failed(ServerStartFailure(attempt))
      case _ =>
        val port = newPort
        App.start(env + ("PORT" -> port.toString) + ("HOST" -> "localhost")).recoverWith {
          case _ => newServer(attempt + 1)
        } map { server =>
          recentlyUsedPorts.add(port)
          server
        }
    }
    newServer()
  }

  private def newPort = {
    val available: IndexedSeq[Int] = portRange.diff(recentlyUsedPorts.toSeq)
    available(Random.nextInt(available.size))
  }

  private lazy val recentlyUsedPorts: collection.mutable.Set[Int] = collection.mutable.Set.empty
  private lazy val portRange = 9000 to 9999
  private lazy val maxStartupAttempts = 5
}
