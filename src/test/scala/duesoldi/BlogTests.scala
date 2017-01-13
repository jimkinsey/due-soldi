package duesoldi

import org.scalatest.AsyncFunSpec

class BlogTests extends AsyncFunSpec {
  import org.scalatest.Matchers._
  import duesoldi.testapp.TestAppRequest.get

  describe("getting a non-existent blog entry") {

    it("responds with a 404") {
      get("/blog/what-i-had-for-breakfast") { _.status shouldBe 404 }
    }

  }

}
