package cicerone.test

import cicerone.HttpConnection.Configuration
import cicerone._
import cicerone.test.support.TestServer._
import hammerspace.testing.CustomMatchers._
import hammerspace.testing.StreamHelpers._
import ratatoskr.Method.{GET, POST, HEAD}
import ratatoskr.RequestBuilding._
import utest._

import scala.concurrent.duration._

object ClientTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "A client" - {
      "returns a failure when it cannot connect" - {
        val result = new Client(Configuration(connectTimeout = 1.millisecond)) send GET("http://127.0.0.1:1")
        result map { res =>
          assert(res isLeftOf ConnectionFailure)
        }
      }
      "returns a failure when the URL is malformed" - {
        val result = new Client() send GET("ptth\\:blah blah")
        result map { res =>
          assert(res isLeftOf MalformedURL)
        }
      }
      "includes the status code in the response" - {
        withServer { case (GET, "/foo") => _ => (200, "OK") } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/foo")
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "includes headers in the response" - {
        withServer { case (GET, "/headers") => _ => (200, "OK", Map("a" -> Seq("1"))) } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/headers")
          } yield {
            assert(response isRightWhere(_.headers("A") == Seq("1")))
          }
        }
      }
      "includes the body in the response" - {
        withServer { case (GET, "/body") => _ => (200, "OK") } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/body")
          } yield {
            assert(response isRightWhere(_.body.asString == "OK"))
          }
        }
      }
      "does not include the body for a HEAD" - {
        withServer { case (GET, "/body") => _ => (200, "OK") } { server =>
          for {
            response <- new Client() send HEAD(s"http://localhost:${server.port}/body")
          } yield {
            assert(response isRightWhere(_.body.asString == ""))
          }
        }
      }
      "sends request headers" - {
        withServer { case (GET, "/headers") => req => (200, req.headers("The-answer").head) } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/headers").header("The-Answer" -> "42")
          } yield {
            assert(response isRightWhere(_.body.asString == "42"))
          }
        }
      }
      "sends request headers where there are multiple values for a key" - {
        withServer { case (GET, "/headers") => req => (200, req.headers("X").mkString(",")) } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/headers").header("x" -> "42").header("x" -> "43")
          } yield {
            assert(response isRightWhere(_.body.asString == "42,43"))
          }
        }
      }
      "sends the body for a POST" - {
        withServer { case (POST, "/post") => req => (201, req.body.asString) } { server =>
          for {
            response <- new Client() send POST(s"http://localhost:${server.port}/post", "Hello!")
          } yield {
            assert(response isRightWhere(_.body.asString == "Hello!"))
          }
        }
      }
      "includes the response body for a 4XX error" - {
        withServer { case (GET, "/does-not-exist") => _ => (404, "Not Found") } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/does-not-exist")
          } yield {
            assert(response isRightWhere(_.body.asString == "Not Found"))
          }
        }
      }
      "includes the response body for a 5XX error" - {
        withServer { case (GET, "/error") => _ => (500, "Internal Server Error") } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/error")
          } yield {
            assert(response isRightWhere(_.body.asString == "Internal Server Error"))
          }
        }
      }
      "does not follow redirects by default" - {
        withServer { case (GET, "/redirect") => _ => (302, "Location" -> "/other") } { server =>
          for {
            response <- new Client() send GET(s"http://localhost:${server.port}/redirect")
          } yield {
            assert(response isRightWhere(_.status == 302))
          }
        }
      }
      "follows redirects when configured to" - {
        withServer {
          case (GET, "/redirect") => _ => (302, "Location" -> "/other")
          case (GET, "/other") => _ => (200, "Redirected!")
        } { server =>
          for {
            response <- new Client(Configuration(followRedirects = true)) send GET(s"http://localhost:${server.port}/redirect")
          } yield {
            assert(response isRightWhere(_.body.asString == "Redirected!"))
          }
        }
      }
    }
  }
}
