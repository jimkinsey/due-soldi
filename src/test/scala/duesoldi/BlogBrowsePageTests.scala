package duesoldi

import duesoldi.pages.{BlogEntryPage, BlogIndexPage}
import duesoldi.storage.BlogStorage
import org.scalatest.AsyncFunSpec

class BlogBrowsePageTests extends AsyncFunSpec with BlogStorage {

  import duesoldi.testapp.TestAppRequest.get
  import org.scalatest.Matchers._

  describe("when there are no blog entries") {

    it("responds with a 404") {
      get("/blog/") {
        _.status shouldBe 404
      }
    }

  }

  describe("when there are only invalid blog entries") {

    it("responds with a 404") {
      withBlogEntries(
        "invalid-content" -> "boom",
        "InVALid ID" -> "# Boom") { implicit config =>
        get("/blog/") {
          _.status shouldBe 404
        }
      }
    }

  }

  describe("when there is a mix of valid and invalid blog entries") {

    it("filters out the invalid entries") {
      withBlogEntries(
        "invalid-content" -> "boom",
        "InVALid ID" -> "# Boom",
        "valid-content-and-id" -> "# Hello!") { implicit config =>
        get("/blog/") { response =>
          response.status shouldBe 200
          val page = new BlogIndexPage(response.body)
          page.blogEntries should have(size(1))
          page.blogEntries.head should have('link ("/blog/valid-content-and-id"),
                                            'title ("Hello!"))
        }
      }
    }

  }

  describe("the blog index page") {

    it("responds with a 200") {
      withBlogEntries("first-post" -> "# Hello, World!") { implicit config =>
        get("/blog/") { _.status shouldBe 200 }
      }
    }

    it("has content-type text/html") {
      withBlogEntries("year-in-review" -> "# tedious blah") { implicit config =>
        get("/blog/") { _.headers("Content-Type") should contain("text/html; charset=UTF-8") }
      }
    }

    it("has a copyright notice") {
      withBlogEntries("top-content" -> "# this is well worth copyrighting") { implicit config =>
        get("/blog/") { response =>
          new BlogIndexPage(response.body).footer.copyrightNotice shouldBe Some("© 2016-2017 Jim Kinsey")
        }
      }
    }

  }

}

