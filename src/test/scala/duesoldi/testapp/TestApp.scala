package duesoldi.testapp

import duesoldi._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class ServerStartFailure(attempt: Int) extends Exception

object TestApp {

  def runningApp(implicit executionContext: ExecutionContext) = new Setup {
    var server: Server = _
    override def setup(env: Env): Future[Env] = {
      def attemptStart(attempts: Int = 0): Future[Env] = attempts match {
        case n if n > maxStartupAttempts => Future.failed(ServerStartFailure(attempts))
        case _ =>
          val port = newPort
          val newEnv = env + ("PORT" -> port.toString) + ("HOST" -> "localhost") + ("LOGGING_ENABLED" -> "false")
          App
            .start(newEnv)
            .map { s =>
              server = s
              recentlyUsedPorts.add(port)
              newEnv
            }
            .recoverWith {
              case _ => attemptStart(attempts + 1)
            }
      }
      attemptStart()
    }
    override def tearDown: Future[Unit] = {
      server.stop()
    }
  }

  private def newPort = {
    val available: IndexedSeq[Int] = portRange.diff(recentlyUsedPorts.toSeq)
    available(Random.nextInt(available.size))
  }

  private lazy val recentlyUsedPorts: collection.mutable.Set[Int] = collection.mutable.Set.empty
  private lazy val portRange = 9000 to 9999
  private lazy val maxStartupAttempts = 5
}
