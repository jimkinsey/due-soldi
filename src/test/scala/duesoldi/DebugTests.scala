package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.TestApp.runningApp
import duesoldi.testapp.ServerRequests._
import Setup.withSetup
import utest._
import AdminSupport._

object DebugTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "the headers endpoint" - {
      "return a page with the received request headers" - {
        withSetup(
          adminCredentials("admin", "password"),
          runningApp
        ) { implicit env =>
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
