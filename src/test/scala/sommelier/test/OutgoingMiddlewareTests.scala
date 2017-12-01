package sommelier.test

import sommelier.ApplyMiddleware.applyOutgoing
import sommelier.{ApplyMiddleware, Method, Request}
import sommelier.SyncResult.Accepted
import sommelier.Routing._
import sommelier.test.support.CustomMatchers._
import utest._

import scala.concurrent.Future

object OutgoingMiddlewareTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "Applying outgoing middleware to a request / response pair" - {
      "returns a result of the response if there is no outgoing middleware" - {
        val request = Request(method = Method.GET, path = "/")
        val response = 200 ("OK")
        val result = applyOutgoing(Seq.empty)(request, response)
        assert(result == Accepted(response))
      }
      "returns a result of the response for the first matching outgoing middleware" - {
        val middleware = Seq(
          AnyRequest outgoing { (_, res) => res body "Handled" }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/"), 200 ("OK"))
        assert(result isAcceptedWhere (_.body contains "Handled"))
      }
      "does not apply middleware that does not match" - {
        val middleware = Seq(
          GET("/foo") outgoing { (_, res) => res body s"${res.body.getOrElse("")} foo!" },
          GET("/bar") outgoing { (_, res) => res body s"${res.body.getOrElse("")} bar!" }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/bar"), 200 ("OK"))
        assert(result isAcceptedWhere (_.body contains "OK bar!"))
      }
      "applies all matching middleware, in order" - {
        val middleware = Seq(
          GET("/") outgoing { (_, res) => res body s"${res.body.getOrElse("")} 1" },
          GET("/") outgoing { (_, res) => res body s"${res.body.getOrElse("")} 2" }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/"), 200 ("OK"))
        assert(result isAcceptedWhere (_.body contains "OK 1 2"))
      }
      "works with async middleware" - {
        val middleware = Seq(
          GET("/") outgoing { (_, res) => Future { res body "handled!" } }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/"), 200)
        assert(result isAcceptedWhere (_.body contains "handled!"))
      }
    }
  }
}
