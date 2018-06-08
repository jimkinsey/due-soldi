package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.setup.Setup.withSetup
import duesoldi.test.support.setup.SyncSetup
import utest._
import hammerspace.testing.StreamHelpers._

object DebugTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "the headers endpoint" - {
      "return a page with the received request headers" - {
        withSetup(
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/headers", headers = TestApp.adminAuth, "Key" -> "Value")
          } yield {
            assert(response.body.asString.lines.toList contains "Key: Value")
          }
        }
      }
    }
    "the config endpoint" - {
      "return a page containing the env vars" - {
        withSetup(
          envVars("IMAGE_BASE_URL" -> "http://somewhere"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/config", headers = TestApp.adminAuth)
          } yield {
            assert(response.body.asString.lines.toList contains "IMAGE_BASE_URL=http://somewhere")
          }
        }
      }
      "not include sensitive env vars" - {
        withSetup(
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/config", headers = TestApp.adminAuth)
          } yield {
            assert(!(response.body.asString contains "ADMIN_CREDENTIALS"))
          }
        }
      }
    }
  }

  private def envVars(vars: (String, String)*): SyncSetup = _ => vars.toMap
}
