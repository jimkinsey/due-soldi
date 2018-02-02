package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests.get
import duesoldi.test.support.app.TestApp
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.setup.Setup.withSetup
import duesoldi.test.support.setup.SyncSetup
import utest._

object ConfigOverrideTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "sending a Config-Override header without the secret key results in no overrides" - {
      withSetup(
        envVars("IMAGE_BASE_URL" -> "http://somewhere"),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config", headers = TestApp.adminAuth, "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else")
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere"
          )
        }
      }
    }
    "sending a Config-Override header with the correct secret key results in an override" - {
      withSetup(
        envVars(
          "IMAGE_BASE_URL" -> "http://somewhere"
        ),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config",
            headers = TestApp.adminAuth, "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else", "Secret-Key" -> TestApp.secretKey)
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere.else"
          )
        }
      }
    }
    "sending a Config-Override header with an incorrect secret key results in no overrides" - {
      withSetup(
        envVars(
          "IMAGE_BASE_URL" -> "http://somewhere"
        ),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config",
            headers = TestApp.adminAuth, "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else", "Secret-Key" -> "5eCrEt")
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere"
          )
        }
      }
    }
  }

  private def envVars(vars: (String, String)*): SyncSetup = _ => vars.toMap
}
