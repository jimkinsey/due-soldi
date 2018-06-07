package duesoldi.test.support.app

import duesoldi._
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.setup.{AsyncSetup, SyncSetup}
import sommelier.events.ExceptionWhileRouting
import sommelier.serving.Server

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class ServerStartFailure(attempt: Int) extends Exception

object TestApp
{
  val secretKey: String = "test-secret-key"

  val testEnv = Map(
    "PORT" -> "0",
    "HOST" -> "localhost",
    "LOGGING_ENABLED" -> "false",
    "SECRET_KEY" -> secretKey,
    "JDBC_DATABASE_USERNAME" -> "user",
    "JDBC_DATABASE_PASSWORD" -> "password",
    "ADMIN_CREDENTIALS" -> "user:password"
  )

  val adminAuth = BasicAuthorization("user", "password")

  private var running: Server = _

  def app: Server = running

  def attemptStop(implicit executionContext: ExecutionContext): Future[Unit] = Future { running.halt() }

  def attemptStart(implicit executionContext: ExecutionContext): Unit = {
    Await.result(App.start(testEnv)
      .map { s =>
        s.subscribe {
          case ExceptionWhileRouting(_, exception) => exception.printStackTrace()
        }
        running = s
      }, 5.seconds)
  }

  def runningApp(implicit executionContext: ExecutionContext): SyncSetup = new SyncSetup {
    override def setup(env: Env): Env = {
      Map("PORT" -> app.port.toString, "HOST" -> app.host)
    }
  }

  def runningAppForThisTestOnly(implicit executionContext: ExecutionContext): AsyncSetup = new AsyncSetup {
    var myApp: Server = _

    override def setup(env: Env): Future[Env] = {
      App.start(testEnv ++ env ++ Map("PORT" -> "0")).map { s =>
        s.subscribe {
          case ExceptionWhileRouting(_, exception) => exception.printStackTrace()
        }
        myApp = s
        Map("PORT" -> myApp.port.toString, "HOST" -> myApp.host)
      }
    }

    override def tearDown: Future[Unit] = {
      Future { myApp.halt() }
    }
  }
}
