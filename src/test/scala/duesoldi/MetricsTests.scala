package duesoldi

import akka.parboiled2.util.Base64
import duesoldi.Setup.withSetup
import duesoldi.storage.BlogStorage
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._

import scala.concurrent.Future

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
      pending
      withSetup(
        metrics(adminCredentials = "admin:password"),
        blogEntries("id" -> "# Content!")
      ) {
        // TODO need to call the blog entry
        get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "password")) { response =>
          response.status shouldBe 200
          response.body.lines.toList.head shouldBe "Timestamp,Path"
          response.body.lines.toList.tail shouldBe Seq()
          // TODO Need to assert something useful here
        }
      }
    }

    "serve a CSV file containing a row for each blog index page request" in {
      pending
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
