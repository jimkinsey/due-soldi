package cicerone.test

import cicerone._
import cicerone.test.support.CustomMatchers._
import cicerone.test.support.StreamHelpers._
import cicerone.test.support.TestServer.{GET, POST, withServer}

import scala.concurrent.duration._

import utest._

object ClientTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "A client" - {
      "returns a failure when it cannot connect" - {
        val result = new Client(connectTimeout = 1.millisecond).send(http GET "http://127.0.0.3")
        result map { res =>
          assert(res isLeftOf ConnectionFailure)
        }
      }
      "returns a failure when the URL is malformed" - {
        val result = new Client() send(http GET "ptth\\:blah blah")
        result map { res =>
          assert(res isLeftOf MalformedURL)
        }
      }
      "includes the status code in the response" - {
        withServer { case GET("/foo") => (200, "OK") } { server =>
          for {
            response <- new Client() send(http GET s"http://localhost:${server.port}/foo")
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "includes headers in the response" - {
        withServer { case GET("/headers") => (200, "OK", Map("a" -> Seq("1"))) } { server =>
          for {
            response <- new Client() send(http GET s"http://localhost:${server.port}/headers")
          } yield {
            assert(response isRightWhere(_.headers("A") == Seq("1")))
          }
        }
      }
      "includes the body in the response" - {
        withServer { case GET("/body") => (200, "OK") } { server =>
          for {
            response <- new Client() send(http GET s"http://localhost:${server.port}/body")
          } yield {
            assert(response isRightWhere(_.body.asString == "OK"))
          }
        }
      }
      "does not include the body for a HEAD" - {
        withServer { case GET("/body") => (200, "OK") } { server =>
          for {
            response <- new Client() send(http HEAD s"http://localhost:${server.port}/body")
          } yield {
            assert(response isRightWhere(_.body.asString == ""))
          }
        }
      }
      "sends request headers" - {
        withServer { case req @ GET("/headers") => (200, req.headers("The-answer").head) } { server =>
          for {
            response <- new Client() send http.GET(s"http://localhost:${server.port}/headers").header("The-Answer" -> "42")
          } yield {
            assert(response isRightWhere(_.body.asString == "42"))
          }
        }
      }
      "sends request headers where there are multiple values for a key" - {
        withServer { case req @ GET("/headers") => (200, req.headers("X").mkString(",")) } { server =>
          for {
            response <- new Client() send http.GET(s"http://localhost:${server.port}/headers").header("x" -> "42").header("x" -> "43")
          } yield {
            assert(response isRightWhere(_.body.asString == "42,43"))
          }
        }
      }
      "sends the body for a POST" - {
        withServer { case req @ POST("/post") => (201, req.body.getOrElse("")) } { server =>
          for {
            response <- new Client() send(http POST(s"http://localhost:${server.port}/post", "Hello!"))
          } yield {
            assert(response isRightWhere(_.body.asString == "Hello!"))
          }
        }
      }
      "includes the response body for a 4XX error" - {
        withServer { case GET("/does-not-exist") => (404, "Not Found") } { server =>
          for {
            response <- new Client() send(http GET s"http://localhost:${server.port}/does-not-exist")
          } yield {
            assert(response isRightWhere(_.body.asString == "Not Found"))
          }
        }
      }
      "includes the response body for a 5XX error" - {
        withServer { case GET("/error") => (500, "Internal Server Error") } { server =>
          for {
            response <- new Client() send(http GET s"http://localhost:${server.port}/error")
          } yield {
            assert(response isRightWhere(_.body.asString == "Internal Server Error"))
          }
        }
      }
    }
  }
}
