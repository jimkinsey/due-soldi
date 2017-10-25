package duesoldi

import duesoldi.AdminSupport._
import duesoldi.Setup.withSetup
import duesoldi.httpclient.BasicAuthorization
import duesoldi.storage.BlogStorage._
import duesoldi.storage.Database._
import duesoldi.testapp.ServerRequests._
import duesoldi.testapp.TestApp.runningApp
import utest._

import scala.concurrent.Future

object AccessRecordingTests
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the access CSV endpoint" - {
      "returns a 401 for unauthorised access" - {
        withSetup(
          adminCredentials("admin", "password"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "wrong-password"))
          } yield {
            assert(response.status == 401)
          }
        }
      }

      "serves a CSV file with a header when there are no metrics recorded" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            assert(
              response.status == 200,
              response.body.lines.toList.head == "Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code",
              response.body.lines.toList.tail == Nil
            )
          }
        }
      }

      "serves a CSV file containing a row for each blog entry page request" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingEnabled,
          runningApp,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            assert(
              response.body.lines exists(_.contains("/blog/id"))
            )
          }
        }
      }

      "serves a CSV file containing a row for each blog index page request" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingEnabled,
          runningApp,
          blogEntry("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/", headers =
              "Referer" -> "http://altavista.is",
              "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
              "Cf-Connecting-Ip" -> "1.2.3.4",
              "Cf-Ipcountry" -> "IS"
            )
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            assert(response.body.lines exists(_.contains("/blog/")))
          }
        }
      }

      "has no records for periods when access recording is disabled" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingDisabled,
          runningApp,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/")
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            assert(response.body.lines.toList.tail.isEmpty)
          }
        }
      }
    }
  }

  private lazy val accessRecordingEnabled = new Setup {
    override def setup(env: Env): Future[Env] = Future successful Map("ACCESS_RECORDING_ENABLED" -> "true")
  }

  private lazy val accessRecordingDisabled = new Setup {
    override def setup(env: Env): Future[Env] = Future successful Map("ACCESS_RECORDING_ENABLED" -> "false")
  }
}
