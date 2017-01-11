package duesoldi

import org.scalatest.AsyncFunSpec
import org.scalatest.Matchers._

class PingPongTests extends AsyncFunSpec {
  import duesoldi.testapp.TestAppRequest.get

  describe("/ping") {

    it("returns 'pong'") {
      get("/ping") { _.getResponseBody shouldBe "pong" }
    }

  }

  describe("/pong") {

    it("returns 'ping'") {
      get("/pong") { _.getResponseBody shouldBe "ping" }
    }

  }

}


