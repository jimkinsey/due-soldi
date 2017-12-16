package sommelier.test

import sommelier.routing.ApplyMiddleware.applyOutgoing
import sommelier.{Method, Request}
import sommelier.SyncResult.Accepted
import sommelier.routing.Routing._
import sommelier.test.support.CustomMatchers._
import utest._

import scala.concurrent.Future

object OutgoingMiddlewareTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  implicit val bodyToString: Array[Byte] => String = (bytes) => new String(bytes, "UTF-8")
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
        assert(result isAcceptedWhere (_.body[String] contains "Handled"))
      }
      "does not apply middleware that does not match" - {
        val middleware = Seq(
          GET("/foo") outgoing { (_, res) => res body s"${res.body[String].getOrElse("")} foo!" },
          GET("/bar") outgoing { (_, res) => res body s"${res.body[String].getOrElse("")} bar!" }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/bar"), 200 ("OK"))
        assert(result isAcceptedWhere (_.body[String] contains "OK bar!"))
      }
      "applies all matching middleware, in order" - {
        val middleware = Seq(
          GET("/") outgoing { (_, res) => res body s"${res.body[String].getOrElse("")} 1" },
          GET("/") outgoing { (_, res) => res body s"${res.body[String].getOrElse("")} 2" }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/"), 200 ("OK"))
        assert(result isAcceptedWhere (_.body[String] contains "OK 1 2"))
      }
      "works with async middleware" - {
        val middleware = Seq(
          GET("/") outgoing { (_, res) => Future { res body "handled!" } }
        )
        val result = applyOutgoing(middleware)(Request(Method.GET, "/"), 200)
        assert(result isAcceptedWhere (_.body[String] contains "handled!"))
      }
    }
  }
}
