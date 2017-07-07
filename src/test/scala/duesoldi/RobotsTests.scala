package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.testapp.{ServerRequests, ServerSupport}

import utest._

object RobotsTests
  extends TestSuite
  with ServerSupport
  with ServerRequests
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the robots.txt endpoint" - {
      "returns an extremely permissive file" - {
        withSetup() {
          withServer { implicit server =>
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
}
