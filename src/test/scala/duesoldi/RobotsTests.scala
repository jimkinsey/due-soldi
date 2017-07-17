package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.testapp.TestApp.runningApp
import duesoldi.testapp.ServerRequests._

import utest._

object RobotsTests
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the robots.txt endpoint" - {
      "returns an extremely permissive file" - {
        withSetup(
          runningApp
        ) { implicit env =>
          for {
            response <- get("/robots.txt")
          } yield {
            assert(
              response.status == 200,
              response.body ==
                """User-agent: *
                  |Disallow:
                  |""".stripMargin,
              response.headers.toSeq.contains("Cache-Control" -> Seq(s"max-age=${24 * 60 * 60}"))
            )
          }
        }
      }
    }
  }
}
