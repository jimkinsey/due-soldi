package sommelier.test

import ratatoskr.Method.GET
import ratatoskr.RequestBuilding._
import ratatoskr.Request
import sommelier.routing.{AuthorizationMatcher, Basic, Rejection}
import sommelier.routing.Routing._
import utest._

object AuthorizationMatcherTests
extends TestSuite
{
  val tests = this {
    "A chained authorization matcher" - {
      "falls back on the second matcher when the first fails" - {
        val jimAuth = Basic("jim", "p455w0rd", "admin")
        val charleyAuth = Basic("charley", "234asd1123", "admin")
        val rejection = (jimAuth or charleyAuth).rejects(GET("/") header "Authorization" -> s"Basic ${charleyAuth.encoded}")
        assert(rejection isEmpty)
      }
      "stops checking matchers after first success" - {
        val goodAuth = new AuthorizationMatcher {
          override def rejects(request: Request): Option[Rejection] = None
        }
        val badAuth = new AuthorizationMatcher {
          override def rejects(request: Request): Option[Rejection] = ???
        }
        val rejection = (goodAuth or badAuth).rejects(GET("/"))
        assert(rejection isEmpty)
      }
    }
  }
}
