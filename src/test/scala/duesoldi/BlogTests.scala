package duesoldi

import duesoldi.storage.BlogStorage
import org.scalatest.AsyncFunSpec

class BlogTests extends AsyncFunSpec with BlogStorage {
  import duesoldi.testapp.TestAppRequest.get
  import org.scalatest.Matchers._

  describe("getting a non-existent blog entry") {

    it("responds with a 404") {
      get("/blog/what-i-had-for-breakfast") { _.status shouldBe 404 }
    }

  }

  describe("a blog entry page") {

    it("responds with a 200") {
      withBlogEntries("first-post" -> "Hello, World!") {
        get("/blog/first-post") { _.status shouldBe 200 }
      }
    }

  }

}
