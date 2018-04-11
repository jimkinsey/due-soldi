package cicerone.test

import cicerone.test.support.TestServer._
import cicerone.test.support.CustomMatchers._

import cicerone._

import utest._

object CiceroneTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "Cicerone" - {
      "supports GET requests" - {
        withServer { case GET("/foo") => (200, "OK") } { server =>
          for {
            response <- http GET s"http://localhost:${server.port}/foo"
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "supports POST requests" - {
        withServer { case POST("/foo") => (201, "Created") } { server =>
          for {
            response <- http POST(s"http://localhost:${server.port}/foo", "Hello!")
          } yield {
            assert(response isRightWhere(_.status == 201))
          }
        }
      }
    }
  }
}
