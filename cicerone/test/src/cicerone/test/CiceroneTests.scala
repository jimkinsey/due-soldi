package cicerone.test

import cicerone._
import cicerone.test.support.TestServer._
import hammerspace.testing.CustomMatchers._
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
            response <- http GET s"http://localhost:${server.port}/foo" send
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "supports HEAD requests" - {
        withServer { case GET("/foo") => (200, "OK") } { server =>
          for {
            response <- http HEAD s"http://localhost:${server.port}/foo" send
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
      "supports POST requests" - {
        withServer { case POST("/foo") => (201, "Created") } { server =>
          for {
            response <- http POST(s"http://localhost:${server.port}/foo", "Hello!") send
          } yield {
            assert(response isRightWhere(_.status == 201))
          }
        }
      }
      "supports PUT requests" - {
        withServer { case PUT("/foo") => (201, "Created") } { server =>
          for {
            response <- http PUT(s"http://localhost:${server.port}/foo", "Hello!") send
          } yield {
            assert(response isRightWhere(_.status == 201))
          }
        }
      }
      "supports DELETE requests" - {
        withServer { case DELETE("/foo") => (204, "") } { server =>
          for {
            response <- http DELETE s"http://localhost:${server.port}/foo" send
          } yield {
            assert(response isRightWhere(_.status == 204))
          }
        }
      }
    }

  }
}
