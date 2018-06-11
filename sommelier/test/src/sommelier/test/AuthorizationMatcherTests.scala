package sommelier.test

import ratatoskr.Method.GET
import ratatoskr.Request
import ratatoskr.RequestBuilding._
import sommelier.Response
import sommelier.routing.{AuthorizationMatcher, Rejection}
import utest._

object AuthorizationMatcherTests
extends TestSuite
{
  val tests = this {
    "A chained authorization matcher" - {
      "falls back on the second matcher when the first fails" - {
        val rejection = (Fails("First") or Passes).rejects(GET("/"))
        assert(rejection isEmpty)
      }
      "stops checking matchers after first success" - {
        val rejection = (Passes or Fails("Bad")).rejects(GET("/"))
        assert(rejection isEmpty)
      }
      "fails with the first error when both fail" - {
        val rejection = (Fails("First") or Fails("Second")).rejects(GET("/"))
        assert(rejection contains TestRejection("First"))
      }
      "passes if both pass" - {
        val rejection = (Passes or Passes).rejects(GET("/"))
        assert(rejection isEmpty)
      }
    }
  }

  object Passes extends AuthorizationMatcher
  {
    override def rejects(request: Request): Option[Rejection] = None
  }

  case class Fails(message: String) extends AuthorizationMatcher
  {
    override def rejects(request: Request): Option[Rejection] = Some(TestRejection(message))
  }

  case class TestRejection(message: String) extends Rejection
  {
    override def response: Response = ???
  }
}
