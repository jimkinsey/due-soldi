package duesoldi

import duesoldi.AdminSupport.adminCredentials
import duesoldi.Setup.withSetup
import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.ServerRequests.get
import duesoldi.testapp.TestApp.runningApp
import utest._

import scala.concurrent.Future

object ConfigOverrideTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "sending a Config-Override header without the secret key results in no overrides" - {
      withSetup(
        adminCredentials("admin", "password"),
        envVars("IMAGE_BASE_URL" -> "http://somewhere"),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config", headers = BasicAuthorization("admin", "password"), "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else")
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere"
          )
        }
      }
    }
    "sending a Config-Override header with the correct secret key results in an override" - {
      withSetup(
        adminCredentials("admin", "password"),
        envVars(
          "IMAGE_BASE_URL" -> "http://somewhere",
          "SECRET_KEY" -> "s3cr3t"
        ),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config",
            headers = BasicAuthorization("admin", "password"), "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else", "Secret-Key" -> "s3cr3t")
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere.else"
          )
        }
      }
    }
    "sending a Config-Override header with an incorrect secret key results in no overrides" - {
      withSetup(
        adminCredentials("admin", "password"),
        envVars(
          "IMAGE_BASE_URL" -> "http://somewhere",
          "SECRET_KEY" -> "s3cr3t"
        ),
        runningApp
      ) { implicit env =>
        for {
          response <- get("/admin/debug/config",
            headers = BasicAuthorization("admin", "password"), "Config-Override" -> "IMAGE_BASE_URL=http://somewhere.else", "Secret-Key" -> "5eCrEt")
        } yield {
          assert(
            response.body contains "IMAGE_BASE_URL=http://somewhere"
          )
        }
      }
    }
  }

  private def envVars(vars: (String, String)*): Setup = (env: Env) => Future.successful(vars.toMap)
}
