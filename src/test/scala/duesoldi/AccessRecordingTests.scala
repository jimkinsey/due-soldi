package duesoldi

import java.io.StringReader
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import com.github.tototoshi.csv.CSVReader
import duesoldi.Setup.withSetup
import duesoldi.httpclient.BasicAuthorization
import duesoldi.scalatest.CustomMatchers
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

class AccessRecordingTests extends AsyncWordSpec with BlogStorage with ServerSupport with CustomMatchers with ServerRequests with Database with AdminSupport {

  "the access CSV endpoint" must {

    "return a 401 for unauthorised access" in {
      withSetup(adminCredentials("admin", "password")) {
        withServer { implicit server =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "wrong-password"))
          } yield {
            response.status shouldBe 401
          }
        }
      }
    }

    "serve a CSV file with a header when there are no metrics recorded" in {
      withSetup(
        database,
        adminCredentials("admin", "password")) {
        withServer { implicit server =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            response.status shouldBe 200
            response.body.lines.toList.head shouldBe "Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code"
            response.body.lines.toList.tail shouldBe empty
          }
        }
      }
    }

    "serve a CSV file containing a row for each blog entry page request" in {
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
            response.body.lines.size shouldBe 2
            val fields = response.body.lines.toList.tail.head.split(",")
            fields(0) shouldBe parsableAs(ISO_DATE_TIME)
            fields(1) shouldBe "/blog/id"
          }
        }
      }
    }

    "serve a CSV file containing a row for each blog index page request" in {
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
              "cf-connecting-ip" -> "1.2.3.4",
              "cf-ipcountry"     -> "IS"
            )
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            val content = CSVReader.open(new StringReader(response.body)).allWithHeaders()
            content.size                shouldBe 1
            content(0)("Timestamp")     shouldBe parsableAs(ISO_DATE_TIME)
            content(0)("Path")          shouldBe "/blog/"
            content(0)("Referer")       shouldBe "http://altavista.is"
            content(0)("User-Agent")    shouldBe "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0"
            content(0)("Duration (ms)") shouldBe aValidLong
            content(0)("Duration (ms)").toInt should be > 0
            content(0)("Client IP")     shouldBe "1.2.3.4"
            content(0)("Country")       shouldBe "IS"
            content(0)("Status Code")   shouldBe "200"
          }
        }
      }
    }

    "have no records for periods when access recording is disabled" in {
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
            response.body.lines.toList.tail.size shouldBe 0
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
