package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._

class RobotsTests extends AsyncWordSpec with ServerSupport with ServerRequests {

  "the robots.txt endpoint" must {

    "return an extremely permissive file" in {
      withSetup() {
        withServer { implicit server =>
          for {
            response <- get("/robots.txt")
          } yield {
            response.status shouldBe 200
            response.body shouldBe
              """User-agent: *
                |Disallow:
                |""".stripMargin
            response.headers should contain("Cache-Control" -> Seq(s"max-age=${24 * 60 * 60}"))
          }
        }
      }
    }

  }

}
