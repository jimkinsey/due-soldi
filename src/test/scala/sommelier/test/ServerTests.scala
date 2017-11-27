package sommelier.test

import dispatch.{Http, url}
import sommelier.Server
import utest._

import scala.util.Try

object ServerTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
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
    }
  }

  def withServer[T](server: => Try[Server])(block: Server => T): T = {
    val res = server.map(block).getOrElse(???)
    server.map(_.halt())
    res
  }
}
