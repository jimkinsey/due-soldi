package duesoldi

import java.io.StringReader
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import com.github.tototoshi.csv.CSVReader
import duesoldi.Setup.withSetup
import duesoldi.httpclient.BasicAuthorization
import duesoldi.storage.BlogStorage._
import duesoldi.testapp.ServerSupport._
import duesoldi.testapp.ServerRequests._
import duesoldi.storage.Database._
import AdminSupport._
import test.matchers.CustomMatchers._

import scala.concurrent.{ExecutionContext, Future}
import utest._

object AccessRecordingTests
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the access CSV endpoint" - {
      "returns a 401 for unauthorised access" - {
        withSetup(adminCredentials("admin", "password")) {
          withServer { implicit server =>
            for {
              response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "wrong-password"))
            } yield {
              assert(response.status == 401)
            }
          }
        }
      }

      "serves a CSV file with a header when there are no metrics recorded" - {
        withSetup(
          database,
          adminCredentials("admin", "password")) {
          withServer { implicit server =>
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
      }

      "serves a CSV file containing a row for each blog entry page request" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingEnabled,
          blogEntries("id" -> "# Content!")
        ) {
          withServer { implicit server: Server =>
            for {
              _        <- get("/blog/id")
              response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(response.body.lines.size == 2)
              val fields = response.body.lines.toList.tail.head.split(",")
              assert(
                fields(0) hasDateFormat ISO_DATE_TIME,
                fields(1) == "/blog/id"
              )
            }
          }
        }
      }

      "serves a CSV file containing a row for each blog index page request" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingEnabled,
          blogEntries("id" -> "# Content!")
        ) {
          withServer { implicit server: Server =>
            for {
              _        <- get("/blog/", headers =
                "Referer"          -> "http://altavista.is",
                "User-Agent"       -> "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
                "Cf-Connecting-Ip" -> "1.2.3.4",
                "Cf-Ipcountry"     -> "IS"
              )
              response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
            } yield {
              val content = CSVReader.open(new StringReader(response.body)).allWithHeaders()
              assert(
                content.size == 1,
                content(0)("Timestamp") hasDateFormat ISO_DATE_TIME,
                content(0)("Path") == "/blog/",
                content(0)("Referer") == "http://altavista.is",
                content(0)("User-Agent") == "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
                content(0)("Duration (ms)") isAValidLong,
                content(0)("Duration (ms)").toInt > 0,
                content(0)("Client IP") == "1.2.3.4",
                content(0)("Country") == "IS",
                content(0)("Status Code") == "200"
              )
            }
          }
        }
      }

      "has no records for periods when access recording is disabled" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          accessRecordingDisabled,
          blogEntries("id" -> "# Content!")
        ) {
          withServer { implicit server: Server =>
            for {
              _        <- get("/blog/")
              _        <- get("/blog/id")
              response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(response.body.lines.toList.tail.isEmpty)
            }
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
