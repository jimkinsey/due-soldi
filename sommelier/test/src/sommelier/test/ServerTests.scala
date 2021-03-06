package sommelier.test

import java.util.Base64

import cicerone._
import hammerspace.testing.CustomMatchers._
import hammerspace.testing.StreamHelpers._
import ratatoskr.RequestBuilding._
import ratatoskr.ResponseBuilding._
import ratatoskr.{Method, Request}
import sommelier.handling.Unpacking._
import sommelier.routing.Routing._
import sommelier.routing.{Basic, Controller}
import sommelier.serving.Server
import utest._

import scala.concurrent.Future
import scala.util.Try

object ServerTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  implicit val bodyToString: Array[Byte] => String = (bytes) => new String(bytes, "UTF-8")
  val tests = this
  {
    "A Sommelier server" - {
      "returns a 404 when it has no routes" - {
        withServer({ Server.start(routes = Seq.empty) }) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/any-url"))
          } yield {
            assert(response isRightWhere(_.status == 404))
          }
        }
      }
      "returns the handled response of the first matching route" - {
        withServer({ Server.start(routes = Seq(
          GET("/some-other-resource") respond { _ => 200 },
          GET("/some-resource") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "returns a 404 if none of the routes match" - {
        withServer({ Server.start(routes = Seq(
          GET("/some-other-resource") respond { _ => 200 },
          GET("/yet-another-resource") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(response isRightWhere(_.status == 404))
          }
        }
      }
      "prefers to reject by path than by method" - {
        withServer({ Server.start(routes = Seq(
          PUT("/some-other-resource") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(response isRightWhere(_.status == 404))
          }
        }
      }
      "handles HEAD requests" - {
        withServer({ Server.start(routes = Seq(
          GET("/some-resource") respond { _ => 200 ("Some content") }
        ) )}) { server =>
          for {
            response <- http(Request(Method.HEAD, s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(
              response isRightWhere(_.status == 200),
              response isRightWhere(_.body.isEmpty),
              response isRightWhere(_.headers.get("Content-Length").isEmpty)
            )
          }
        }
      }
      "sends the response body" - {
        withServer({ Server.start(routes = Seq(
          GET("/some-resource") respond { _ => 200 ("Some content") }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(
              response isRightWhere(_.body.asString == "Some content"),
              response isRightWhere(_.headers("Content-length").head.toInt == "Some content".getBytes("UTF-8").length)
            )
          }
        }
      }
      "sends the response headers" - {
        withServer({ Server.start(routes = Seq(
          GET("/ask") respond { _ => 200 ("Some content") header ("X-The-Answer" -> "42") }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/ask"))
          } yield {
            assert(
              response isRightWhere(_.headers.get("X-the-answer") contains Seq("42"))
            )
          }
        }
      }
      "allows async request handling" - {
        withServer({ Server.start(routes = Seq(
          GET("/async") respond { _ => Future { 200 ("hi!") } }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/async"))
          } yield {
            assert(
              response isRightWhere(_.status == 200)
            )
          }
        }
      }
      "returns the response from the rejection when the handler rejects" - {
        withServer({ Server.start(routes = Seq(
          GET("/book") respond { _ => reject(451) }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/book"))
          } yield {
            assert(
              response isRightWhere(_.status == 451)
            )
          }
        }
      }
      "returns the response from the rejection when the handler rejects asynchronously" - {
        withServer({ Server.start(routes = Seq(
          GET("/book") respond { _ => Future { reject(451) } }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/book"))
          } yield {
            assert(
              response isRightWhere(_.status == 451)
            )
          }
        }
      }
      "when all routes fail, chooses the most appropriate rejection" - {
        withServer({ Server.start(routes = Seq(
          GET("/path-fail") respond { _ => 200 },
          GET("/path-match-acc-match-auth-fail") Accept "text/plain" Authorization Basic("u", "x", "r") respond { _ => 200 },
          GET("/path-fail-auth-match") Authorization Basic("u", "p", "r") respond { _ => 200 },
          GET("/path-match-acc-fail-auth-match") Accept "text/css" Authorization Basic("u", "p", "r") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/path-match-acc-match-auth-fail")
                .header("Accept", "text/plain")
                .header("Authorization", s"Basic ${Base64.getEncoder.encodeToString("u:p".getBytes())}"))
          } yield {
            assert(
              response isRightWhere(_.status == 403)
            )
          }
        }
      }
      "applies incoming middleware to any matching incoming request" - {
        withServer({ Server.start(
          routes = Seq(
            GET("/path") respond { implicit context => body[String] map (content => 200 (content)) }
          ),
          middleware = Seq(
            AnyRequest incoming { _ content "Handled" }
          )
        )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/path"))
          } yield {
            assert(
              response isRightWhere(_.body.asString == "Handled")
            )
          }
        }
      }
      "applies outgoing middleware to the generated response of any matching request" - {
        withServer({ Server.start(
          routes = Seq(
            GET("/path") respond { _ => 200 }
          ),
          middleware = Seq(
            AnyRequest outgoing { (req, res) => res content "Handled" }
          )
        )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/path"))
          } yield {
            assert(
              response isRightWhere(_.body.asString == "Handled")
            )
          }
        }
      }
      "can be configured with controllers for neater routing" - {
        withServer({ Server.start(
          Seq(new Controller {
            AnyRequest >-- { req => req content "in->" }
            AnyRequest ->- { ctx => 200 content s"${ctx.request.body.asString}handled->" }
            AnyRequest --> { (req, res) => res content s"${res.body.asString}out" }
          })
        )}) { server =>
          for {
            response <- http(Request(Method.GET, s"http://localhost:${server.port}/"))
          } yield {
            assert(
              response isRightWhere(_.body.asString == "in->handled->out")
            )
          }
        }
      }
    }
  }

  def withServer[T](server: => Try[Server])(block: Server => T): T = {
    val res = server.map(block).recover { case e => e.printStackTrace(); ??? } getOrElse ???
    server.map(_.halt())
    res
  }
}
