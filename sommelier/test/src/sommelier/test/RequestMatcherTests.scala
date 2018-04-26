package sommelier.test

import sommelier.messaging.Method.GET
import sommelier.messaging.Request
import sommelier.routing.{BadRequest, RequestMatcher}
import utest._

object RequestMatcherTests
extends TestSuite
{
  val tests = this
  {
    "A request matcher" - {
      "can match on host" - {
        val request = Request(GET, "/").host("sommelier.io")
        val rejection = RequestMatcher().Host("sommelier.io").rejects(request)
        assert(rejection isEmpty)
      }
      "can reject on host" - {
        val request = Request(GET, "/").host("scalatra.org")
        val rejection = RequestMatcher().Host("sommelier.io").rejects(request)
        assert(rejection contains BadRequest)
      }
    }
  }
}
