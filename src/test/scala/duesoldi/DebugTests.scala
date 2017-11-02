package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.TestApp.runningApp
import duesoldi.testapp.ServerRequests._
import Setup.withSetup
import utest._
import AdminSupport._

import scala.concurrent.Future

object DebugTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "the headers endpoint" - {
      "return a page with the received request headers" - {
        withSetup(
          adminCredentials("user", "password"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/headers", headers = BasicAuthorization("user", "password"), "Key" -> "Value")
          } yield {
            assert(response.body.lines.toList contains "Key: Value")
          }
        }
      }
    }
    "the config endpoint" - {
      "return a page containing the env vars" - {
        withSetup(
          envVars("IMAGE_BASE_URL" -> "http://somewhere"),
          adminCredentials("user", "password"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/config", headers = BasicAuthorization("user", "password"))
          } yield {
            assert(response.body.lines.toList contains "IMAGE_BASE_URL=http://somewhere")
          }
        }
      }
      "not include sensitive env vars" - {
        withSetup(
          adminCredentials("user", "password"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/admin/debug/config", headers = BasicAuthorization("user", "password"))
          } yield {
            assert(!(response.body contains "ADMIN_CREDENTIALS"))
          }
        }
      }
    }
  }

  private def envVars(vars: (String, String)*): Setup = (env: Env) => Future.successful(vars.toMap)
}
