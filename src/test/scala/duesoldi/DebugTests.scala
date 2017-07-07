package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.{ServerRequests, ServerSupport}
import Setup.withSetup
import utest._

object DebugTests
  extends TestSuite
  with ServerSupport
  with ServerRequests
  with AdminSupport
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "the headers endpoint" - {
      "return a page with the received request headers" - {
        withSetup(adminCredentials("admin", "password")) {
          withServer { implicit server =>
            for {
              response <- get("/admin/debug/headers", headers = BasicAuthorization("admin", "password"), "Key" -> "Value")
            } yield {
              assert(response.body.lines.toList contains "Key: Value")
            }
          }
        }
      }
    }
  }
}
