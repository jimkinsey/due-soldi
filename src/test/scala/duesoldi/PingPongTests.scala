package duesoldi

import duesoldi.httpclient.HttpClient
import duesoldi.testapp.TestApp
import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

class PingPongTests extends AsyncFunSpec {

  describe("/ping") {

    it("returns 'pong'") {
      for {
        server <- TestApp.start
        res    <- HttpClient.get("/ping", server)
        _      <- TestApp stop server
      } yield {
        res.getResponseBody shouldBe "pong"
      }
    }

  }

  describe("/pong") {

    it("returns 'ping'") {
      for {
        server <- TestApp.start
        res    <- HttpClient.get("/pong", server)
        _      <- TestApp stop server
      } yield {
        res.getResponseBody shouldBe "ping"
      }
    }

  }

}


