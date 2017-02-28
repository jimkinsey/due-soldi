package duesoldi

import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import akka.parboiled2.util.Base64
import duesoldi.Setup.withSetup
import duesoldi.scalatest.CustomMatchers
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

class AccessRecordingTests extends AsyncWordSpec with BlogStorage with ServerSupport with CustomMatchers with ServerRequests with Database {

  "the access CSV endpoint" must {

    "return a 401 for unauthorised access" in {
      withSetup(metrics(adminCredentials = "admin:password")) {
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
        metrics(adminCredentials = "admin:password")) {
        withServer { implicit server =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            response.status shouldBe 200
            response.body.lines.toList.head shouldBe "Timestamp,Path"
            response.body.lines.toList.tail shouldBe empty
          }
        }
      }
    }

    "serve a CSV file containing a row for each blog entry page request" in {
      withSetup(
        database,
        metrics(adminCredentials = "admin:password"),
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
        metrics(adminCredentials = "admin:password"),
        blogEntries("id" -> "# Content!")
      ) {
        withServer { implicit server: Server =>
          for {
            _        <- get("/blog/")
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            response.body.lines.size shouldBe 2
            val fields = response.body.lines.toList.tail.head.split(",")
            fields(0) shouldBe parsableAs(ISO_DATE_TIME)
            fields(1) shouldBe "/blog/"
          }
        }
      }
    }

    "have no records for periods when access recording is disabled" in {
      withSetup(
        database,
        metrics(accessRecordingEnabled = false),
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

  "access recording" must {

    "not prevent page loading if it fails due to database connection problems" in {
      withSetup(
        noDatabase,
        metrics(accessRecordingEnabled = true),
        blogEntries("id" -> "# Content!")
      ) {
        withServer { implicit server: Server =>
          for {
            response <- get("/blog/id")
          } yield {
            response.status shouldBe 200
          }
        }
      }
    }

  }

  private def metrics(adminCredentials: String = "admin:password", accessRecordingEnabled: Boolean = true) = new Setup {
    override def setup: Future[Env] = {
      Future {
        Map(
          "ADMIN_CREDENTIALS" -> adminCredentials,
          "ACCESS_RECORDING_ENABLED" -> accessRecordingEnabled.toString
        )
      }
    }
  }

  object BasicAuthorization {
    def apply(username: String, password: String): (String, String) = "Authorization" -> s"Basic ${Base64.rfc2045().encodeToString(s"$username:$password".getBytes(), false)}"
  }

}
