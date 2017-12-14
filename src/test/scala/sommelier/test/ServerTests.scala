package sommelier.test

import java.nio.charset.Charset
import java.util.Base64

import dispatch.{Http, url}
import sommelier._
import sommelier.Routing._
import utest._

import scala.concurrent.Future
import scala.util.Try
import Unpacking._

object ServerTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  implicit val bodyToString: Array[Byte] => String = (bytes) => new String(bytes, "UTF-8")
  val tests = this
  {
    "A Sommelier server" - {
      "returns a 404 when it has no routes" - {
        withServer({ sommelier.Server.start(routes = Seq.empty) }) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/any-url"))
          } yield {
            assert(response.getStatusCode == 404)
          }
        }
      }
      "returns the handled response of the first matching route" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/some-other-resource") respond { _ => 200 },
          GET("/some-resource") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(response.getStatusCode == 200)
          }
        }
      }
      "returns a 404 if none of the routes match" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/some-other-resource") respond { _ => 200 },
          GET("/yet-another-resource") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(response.getStatusCode == 404)
          }
        }
      }
      "handles HEAD requests" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/some-resource") respond { _ => 200 ("Some content") }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/some-resource").setMethod("HEAD"))
          } yield {
            assert(
              response.getStatusCode == 200,
              response.hasResponseBody == false,
              response.getHeaders("Content-Length").isEmpty
            )
          }
        }
      }
      "sends the response body" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/some-resource") respond { _ => 200 ("Some content") }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/some-resource"))
          } yield {
            assert(
              response.getResponseBody(Charset.forName("UTF-8")) == "Some content",
              response.getHeaders("Content-Length").get(0).toInt == "Some content".getBytes("UTF-8").length
            )
          }
        }
      }
      "sends the response headers" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/ask") respond { _ => 200 ("Some content") header ("X-The-Answer" -> "42") }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/ask"))
          } yield {
            assert(
              response.getHeader("X-The-Answer") == "42"
            )
          }
        }
      }
      "allows async request handling" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/async") respond { _ => Future { 200 ("hi!") } }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/async"))
          } yield {
            assert(
              response.getStatusCode == 200
            )
          }
        }
      }
      "returns the response from the rejection when the handler rejects" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/book") respond { _ => reject(451) }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/book"))
          } yield {
            assert(
              response.getStatusCode == 451
            )
          }
        }
      }
      "returns the response from the rejection when the handler rejects asynchronously" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/book") respond { _ => Future { reject(451) } }
        ) )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/book"))
          } yield {
            assert(
              response.getStatusCode == 451
            )
          }
        }
      }
      "when all routes fail, chooses the most appropriate rejection" - {
        withServer({ sommelier.Server.start(routes = Seq(
          GET("/path-fail") respond { _ => 200 },
          GET("/path-match-acc-match-auth-fail") Accept "text/plain" Authorization Basic("u", "x", "r") respond { _ => 200 },
          GET("/path-fail-auth-match") Authorization Basic("u", "p", "r") respond { _ => 200 },
          GET("/path-match-acc-fail-auth-match") Accept "text/css" Authorization Basic("u", "p", "r") respond { _ => 200 }
        ) )}) { server =>
          for {
            response <- Http.default(
              url(s"http://localhost:${server.port}/path-match-acc-match-auth-fail")
                .setHeader("Accept", "text/plain")
                .setHeader("Authorization", s"Basic ${Base64.getEncoder.encodeToString("u:p".getBytes())}")
            )
          } yield {
            assert(
              response.getStatusCode == 403
            )
          }
        }
      }
      "applies incoming middleware to any matching incoming request" - {
        withServer({ sommelier.Server.start(
          routes = Seq(
            GET("/path") respond { implicit context => body[String] map (content => 200 (content)) }
          ),
          middleware = Seq(
            AnyRequest incoming { _ body "Handled" }
          )
        )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/path"))
          } yield {
            assert(
              response.getResponseBody == "Handled"
            )
          }
        }
      }
      "applies outgoing middleware to the generated response of any matching request" - {
        withServer({ sommelier.Server.start(
          routes = Seq(
            GET("/path") respond { _ => 200 }
          ),
          middleware = Seq(
            AnyRequest outgoing { (req, res) => res body "Handled" }
          )
        )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/path"))
          } yield {
            assert(
              response.getResponseBody == "Handled"
            )
          }
        }
      }
      "can be configured with controllers for neater routing" - {
        withServer({ sommelier.Server.start(
          Seq(new Controller {
            AnyRequest >-- { req => req body "in->" }
            AnyRequest ->- { ctx => 200 body s"${ctx.request.body.getOrElse("")}handled->" }
            AnyRequest --> { (req, res) => res body s"${res.body[String].getOrElse("")}out" }
          })
        )}) { server =>
          for {
            response <- Http.default(url(s"http://localhost:${server.port}/"))
          } yield {
            assert(
              response.getResponseBody == "in->handled->out"
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
