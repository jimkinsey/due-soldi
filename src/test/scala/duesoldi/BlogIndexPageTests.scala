package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogIndexPage
import duesoldi.storage.BlogStorage
import org.scalatest.AsyncFunSpec

class BlogIndexPageTests extends AsyncFunSpec with BlogStorage {

  import duesoldi.testapp.TestAppRequest.get
  import org.scalatest.Matchers._

  describe("visiting 'blog' without the trailing slash") {

    it("redirects to the version with the trailing slash") {
      withSetup(blogEntries("id" -> "# Content!")) {
        get("/blog") { response =>
          response.status shouldBe 301
          response.headers("Location") shouldBe List("/blog/")
        }
      }
    }

  }

  describe("when there are no blog entries") {

    it("responds with a 404") {
      withSetup(blogEntries()) {
        get("/blog/") {
          _.status shouldBe 404
        }
      }
    }

  }

  describe("when there are only invalid blog entries") {

    it("responds with a 404") {
      withSetup(blogEntries(
        "invalid-content" -> "boom",
        "InVALid ID" -> "# Boom")) {
        get("/blog/") {
          _.status shouldBe 404
        }
      }
    }

  }

  describe("when there is a mix of valid and invalid blog entries") {

    it("filters out the invalid entries") {
      withSetup(blogEntries(
        "invalid-content" -> "boom",
        "InVALid ID" -> "# Boom",
        "valid-content-and-id" -> "# Hello!")) {
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
      withSetup(blogEntries("first-post" -> "# Hello, World!")) {
        get("/blog/") { _.status shouldBe 200 }
      }
    }

    it("has content-type text/html") {
      withSetup(blogEntries("year-in-review" -> "# tedious blah")) {
        get("/blog/") { _.headers("Content-Type") should contain("text/html; charset=UTF-8") }
      }
    }

    it("has a copyright notice") {
      withSetup(blogEntries("top-content" -> "# this is well worth copyrighting")) {
        get("/blog/") { response =>
          new BlogIndexPage(response.body).footer.copyrightNotice shouldBe Some("© 2016-2017 Jim Kinsey")
        }
      }
    }

    it("has a title and heading") {
      withSetup(blogEntries("content" -> "# _Content_, mofos")) {
        get("/blog/") { response =>
          val page: BlogIndexPage = new BlogIndexPage(response.body)
          page.title shouldBe "Jim Kinsey's Blog"
          page.heading shouldBe "Latest Blog Entries"
        }
      }
    }

    it("lists the entries in reverse order of last modification") {
      withSetup(blogEntries(
        ("2010-10-12T17:05:00Z", "first-post", "# First"),
        ("2012-12-03T09:34:00Z", "tricky-second-post", "# Second"),
        ("2016-04-01T09:45:00Z", "sorry-for-lack-of-updates", "# Third"))) {
        get("/blog/") { response =>
          val page = new BlogIndexPage(response.body)
          page.blogEntries.map(_.title) shouldBe Seq("Third", "Second", "First")
        }
      }
    }

    it("includes the last modified date in the entry") {
      withSetup(blogEntries(("2010-10-12T17:05:00Z", "dated", "# Dated!"))) {
        get("/blog/") { response =>
          val page = new BlogIndexPage(response.body)
          page.blogEntries.head should have('date ("Tuesday, 12 October 2010"))
        }
      }
    }

    it("has a bio section with a title and some text") {
      withSetup(blogEntries("year-in-review" -> "# Year in review!")) {
        get("/blog/") { response =>
          val page = new BlogIndexPage(response.body)
          page.blurb should have('title ("About"))
          page.blurb.paragraphs should not(be(empty))
        }
      }
    }

  }

}

