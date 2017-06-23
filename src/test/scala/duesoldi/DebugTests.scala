package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec

class DebugTests extends AsyncWordSpec with ServerSupport with ServerRequests with AdminSupport {
  import Setup.withSetup
  import org.scalatest.Matchers._

  "the headers endpoint" must {

    "return a page with the received request headers" in {
      withSetup(adminCredentials("admin", "password")) {
        withServer { implicit server =>
          for {
            response <- get("/admin/debug/headers", headers = BasicAuthorization("admin", "password"), "Key" -> "Value")
          } yield {
            response.body.lines.toList should contain("Key: Value")
          }
        }
      }
    }

  }

}
