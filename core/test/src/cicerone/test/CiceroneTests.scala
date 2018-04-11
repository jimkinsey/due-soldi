package cicerone.test

import cicerone.test.support.TestServer.withServer
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
        withServer { case ("GET", "/foo") => (200, "OK") } { server =>
          for {
            response <- http GET s"http://localhost:${server.port}/foo"
          } yield {
            assert(response isRightWhere(_.status == 200))
          }
        }
      }
    }
  }
}
