package duesoldi.test.support.app

import duesoldi._
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.setup.SyncSetup

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

case class ServerStartFailure(attempt: Int) extends Exception

class TestAppFramework extends utest.runner.Framework
{
  implicit val executionContext: ExecutionContext = utest.framework.ExecutionContext.RunNow
  override def setup(): Unit = {
    println("Starting test app...")
    TestApp.attemptStart
  }
  override def teardown(): Unit = {
    TestApp.attemptStop()
  }
}

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

  def attemptStop(): Future[Unit] = running.stop()

  def attemptStart(implicit executionContext: ExecutionContext): Unit = {
    Await.result(App.start(testEnv)
      .map { s =>
        running = s
      }, 5.seconds)
  }

  def runningApp(implicit executionContext: ExecutionContext) = new SyncSetup {
    override def setup(env: Env): Env = {
      Map("PORT" -> app.port.toString, "HOST" -> app.host)
    }
  }
}
