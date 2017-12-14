package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests.get
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.setup.Setup.withSetup
import utest.{TestSuite, assert, _}

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
            response.headers("Content-type") contains "text/html; charset=UTF-8"
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
    "the learn Japanese end-point redirects if trailing slash is missing" - {
      withSetup(runningApp) { implicit env =>
        for {
          response <- get("/learn-japanese")
        } yield {
          assert(
            response.status == 301,
            response.headers("Location").head endsWith "/learn-japanese/"
          )
        }
      }
    }
  }
}
