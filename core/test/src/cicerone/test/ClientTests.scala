package cicerone.test

import cicerone._
import cicerone.test.support.CustomMatchers._
import cicerone.test.support.StreamHelpers._
import cicerone.test.support.TestServer.{GET, POST, withServer}

import utest._

object ClientTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "A client" - {
      "returns a failure when it cannot connect" - {
        val result = new Client() GET "http://127.0.0.3"
        result map { res =>
          assert(res isLeftOf ConnectionFailure)
        }
      }
      "includes the status code in the response" - {
        withServer { case GET("/foo") => (200, "OK") } { server =>
          for {
            response <- new Client() GET s"http://localhost:${server.port}/foo"
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "includes headers in the response" - {
        withServer { case GET("/headers") => (200, "OK", Map("a" -> Seq("1"))) } { server =>
          for {
            response <- new Client() GET s"http://localhost:${server.port}/headers"
          } yield {
            assert(response isRightWhere(_.headers("A") == Seq("1")))
          }
        }
      }
      "includes the body in the response" - {
        withServer { case GET("/body") => (200, "OK") } { server =>
          for {
            response <- new Client() GET s"http://localhost:${server.port}/body"
          } yield {
            assert(response isRightWhere(_.body.asString == "OK"))
          }
        }
      }
      "sends the body for a POST" - {
        withServer { case req @ POST("/post") => (201, req.body.getOrElse("")) } { server =>
          for {
            response <- new Client() POST(s"http://localhost:${server.port}/post", "Hello!")
          } yield {
            assert(response isRightWhere(_.body.asString == "Hello!"))
          }
        }
      }
    }
  }
}
