package sommelier.test

import ratatoskr.Method.GET
import ratatoskr.Request
import ratatoskr.RequestBuilding._
import sommelier.routing.{BadRequest, RequestMatcher}
import utest._

object RequestMatcherTests
extends TestSuite
{
  val tests = this
  {
    "A request matcher" - {
      "can match on host" - {
        val request = Request(GET, "/").header("Host" -> "sommelier.io")
        val rejection = RequestMatcher().Host("sommelier.io").rejects(request)
        assert(rejection isEmpty)
      }
      "can reject on host" - {
        val request = Request(GET, "/").header("Host" -> "scalatra.org")
        val rejection = RequestMatcher().Host("sommelier.io").rejects(request)
        assert(rejection contains BadRequest)
      }
    }
  }
}
