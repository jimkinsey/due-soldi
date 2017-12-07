package sommelier.test

import sommelier.{Completed, EventBus, ExceptionWhileRouting, HttpMessageContext, Method, Request, Response, Result, Router}
import utest._
import sommelier.Routing._
import sommelier.Unpacking._

import scala.collection.mutable
import scala.concurrent.Future

object RouterTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "A router" - {
      "applies the incoming middleware before handling the request" - {
        val context = new RecordingContext(Request(Method.GET, "/"))
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { implicit req => body[String] map (body => 200(s"$body->")) }
          ),
          middleware = Seq(
            AnyRequest incoming { req => req body "***" }
          )
        )(context)
        assert(context.sent containsA[Response](_.body contains "***->"))
      }
      "applies the outgoing middleware after handling the request" - {
        val context = new RecordingContext(Request(Method.GET, "/"))
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { _ => Future { 200("->") } }
          ),
          middleware = Seq(
            AnyRequest outgoing { case (req, res) => res body s"${res.body.map(b => s"$b***").getOrElse("")}" }
          )
        )(context)
        assert(context.sent containsA[Response](_.body contains "->***"))
      }
      "sends a 500 if there is an exception" - {
        val context = new RecordingContext(Request(Method.GET, "/"))
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { _ => throw new RuntimeException() }
          ),
          middleware = Seq.empty
        )(context)
        assert(context.sent containsA[Response](_.status == 500))
      }
      "sends a 500 if there is an exception (async)" - {
        val context = new RecordingContext(Request(Method.GET, "/"))
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { _ => Future.failed[Result[Response]](new RuntimeException()) }
          ),
          middleware = Seq.empty
        )(context)
        eventually(context.sent containsA[Response](_.status == 500))
      }
      "publishes a message when an exception occurs" - {
        val request = Request(Method.GET, "/")
        val exception = new RuntimeException()
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { _ => throw exception }
          ),
          middleware = Seq.empty
        )(new RecordingContext(request))
        assert(bus.published contains ExceptionWhileRouting(request, exception))
      }
      "publishes a message when a response is sent" - {
        val request = Request(Method.GET, "/")
        implicit val bus: RecordingBus = new RecordingBus()
        Router.complete(
          routes = Seq(
            GET("/") respond { _ => 200("OK") }
          ),
          middleware = Seq.empty
        )(new RecordingContext(request))
        assert(bus.published contains Completed(request, 200("OK")))
      }
    }
  }

  implicit class SeqMatchers[T](seq: Seq[T])
  {
    def containsA[T1 >: T](pred: T1 => Boolean): Boolean = seq.exists(pred)
  }

  class RecordingBus() extends EventBus
  {
    override def publish(event: Any) { published append event }

    lazy val published: mutable.Buffer[Any] = mutable.Buffer()
  }

  class RecordingContext(request: Request) extends HttpMessageContext
  {
    def get: Request = request
    def send(response: Response) { sent append response }

    lazy val sent: mutable.Buffer[Response] = mutable.Buffer()

    override def toString: String = s"sent ${sent.size} response(s): ${sent.mkString("\n---\n")}"
  }
}
