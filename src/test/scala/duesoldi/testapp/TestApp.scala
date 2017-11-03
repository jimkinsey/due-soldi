package duesoldi.testapp

import duesoldi._
import duesoldi.httpclient.BasicAuthorization

import scala.concurrent.{ExecutionContext, Future}

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
    App.start(testEnv)
      .foreach { s =>
        running = s
      }
  }

  def runningApp(implicit executionContext: ExecutionContext) = new SyncSetup {
    override def setup(env: Env): Env = {
      Map("PORT" -> app.port.toString, "HOST" -> app.host)
    }
  }
}
