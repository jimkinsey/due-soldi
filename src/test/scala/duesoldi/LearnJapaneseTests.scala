package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.testapp.ServerRequests.get
import duesoldi.testapp.TestApp.runningApp
import utest.{TestSuite, assert}
import utest._

object LearnJapaneseTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the learn Japanese end-point returns a 200 with HTML content" - {
      withSetup(runningApp) { implicit env =>
        for {
          response <- get("/learn-japanese/")
        } yield {
          assert(
            response.status == 200,
            response.headers("Content-Type") contains "text/html; charset=UTF-8"
          )
        }
      }
    }
    "the learn Japanese end-point serves up JS files, too" - {
      withSetup(runningApp) { implicit env =>
        for {
          response <- get("/learn-japanese/js/app.js")
        } yield {
          assert(
            response.status == 200
          )
        }
      }
    }
  }
}
