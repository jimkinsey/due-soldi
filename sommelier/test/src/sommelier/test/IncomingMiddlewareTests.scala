package sommelier.test

import hammerspace.testing.StreamHelpers._
import ratatoskr.{Method, Request}
import ratatoskr.RequestBuilding._
import sommelier.routing.ApplyMiddleware.applyIncoming
import sommelier.routing.Middleware
import sommelier.routing.Routing._
import sommelier.routing.SyncResult.Accepted
import sommelier.test.support.CustomMatchers._
import utest._

import scala.concurrent.Future

object IncomingMiddlewareTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "Applying incoming middleware to a request" - {
      "returns a result of the request if there is no incoming middleware" - {
        val middleware: Seq[Middleware] = Seq.empty
        val request = Request(Method.GET, "/")
        val result = applyIncoming(middleware)(request)
        assert(result == Accepted(request))
      }
      "returns the result of the single item of incoming middleware if it matches" - {
        val middleware = Seq(
          AnyRequest incoming { _ header "X" -> "32" }
        )
        val result = applyIncoming(middleware)(Request(Method.GET, "/"))
        assert(result isAcceptedWhere (_.headers("X") == Seq("32")))
      }
      "does not apply middleware which does not match" - {
        val middleware = Seq(
          GET ("/foo") incoming { _ header "X" -> "0" },
          GET ("/") incoming { _ header "X" -> "1" },
          POST ("/") incoming { _ header "X" -> "2" }
        )
        val result = applyIncoming(middleware)(Request(Method.GET, "/"))
        assert(result isAcceptedWhere (_.headers("X") == Seq("1")))
      }
      "returns the first rejection" - {
        val middleware = Seq(
          GET ("/") incoming { _ header "X" -> "0" },
          GET ("/") incoming { _ => rejectRequest(401) },
          GET ("/") incoming { _ => rejectRequest(402) }
        )
        val result = applyIncoming(middleware)(Request(Method.GET, "/"))
        assert(result isRejectionAs 401)
      }
      "works with async middleware" - {
        val middleware = Seq(
          GET ("/") incoming { req => Future { req content "42" } }
        )
        val result = applyIncoming(middleware)(Request(Method.GET, "/"))
        assert(result isAcceptedWhere(_.body.asString == "42"))
      }
    }
  }
}
