package duesoldi

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import akka.parboiled2.util.Base64
import duesoldi.Setup.withSetup
import duesoldi.storage.BlogStorage
import duesoldi.testapp.TestApp
import duesoldi.testapp.TestAppRequest.getRaw
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._
import org.scalatest.matchers.{BeMatcher, MatchResult}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MetricsTests extends AsyncWordSpec with BlogStorage {
  import duesoldi.testapp.TestAppRequest.get

  "the metrics CSV endpoint" must {

    "return a 401 for unauthorised access" in {
      withSetup(metrics(adminCredentials = "admin:password")) {
        get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "wrong-password")) { response =>
          response.status shouldBe 401
        }
      }
    }

    "serve a CSV file with a header when there are no metrics recorded" in {
      withSetup(metrics(adminCredentials = "admin:password")) {
        get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password")) { response =>
          response.status shouldBe 200
          response.body.lines.toList.head shouldBe "Timestamp,Path"
          response.body.lines.toList.tail shouldBe empty
        }
      }
    }

    "serve a CSV file containing a row for each blog entry page request" in {
      withSetup(
        metrics(adminCredentials = "admin:password"),
        blogEntries("id" -> "# Content!")
      ) {
        withServer { implicit server: Server =>
          for {
            _        <- getRaw("/blog/id")
            response <- getRaw("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
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
        metrics(adminCredentials = "admin:password"),
        blogEntries("id" -> "# Content!")
      ) {
        withServer { implicit server: Server =>
          for {
            _        <- getRaw("/blog/")
            response <- getRaw("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password"))
          } yield {
            response.body.lines.size shouldBe 2
            val fields = response.body.lines.toList.tail.head.split(",")
            fields(0) shouldBe parsableAs(ISO_DATE_TIME)
            fields(1) shouldBe "/blog/"
          }
        }
      }
    }

  }

  private def parsableAs(format: DateTimeFormatter) = new BeMatcher[String] {
    def apply(in: String) = MatchResult(Try(format.parse(in)).isSuccess, s"[$in] is not parsable in the requested format", s"Succesfully parsed")
  }

  private def withServer[T](block: Server => Future[T])(implicit executionContext: ExecutionContext): Env => Future[T] = (env: Env) => {
    for {
      server <- TestApp.start(env)
      res    <- block(server)
      _      <- server.stop()
    } yield {
      res
    }
  }

  private def metrics(adminCredentials: String) = new Setup {
    override def setup: Future[Env] = Future.successful(Map("ADMIN_CREDENTIALS" -> adminCredentials))
    override def tearDown: Future[Unit] = Future.successful({})
  }

  object BasicAuthorization {
    def apply(username: String, password: String): (String, String) = "Authorization" -> s"Basic ${Base64.rfc2045().encodeToString(s"$username:$password".getBytes(), false)}"
  }

}
