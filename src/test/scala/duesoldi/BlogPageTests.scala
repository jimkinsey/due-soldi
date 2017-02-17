package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogEntryPage
import duesoldi.storage.BlogStorage
import org.scalatest.AsyncFunSpec

class BlogPageTests extends AsyncFunSpec with BlogStorage {
  import duesoldi.testapp.TestAppRequest.get
  import org.scalatest.Matchers._

  describe("getting a non-existent blog entry") {

    it("responds with a 404") {
      withSetup(blogEntries()) {
        get("/blog/what-i-had-for-breakfast") {  _.status shouldBe 404 }
      }
    }

  }

  describe("getting an invalid blog entry") {

    it("responds with a 500") {
      withSetup(blogEntries("no-title" -> "boom")) {
        get("/blog/no-title") { _ .status shouldBe 500 }
      }
    }

  }

  describe("getting a blog entry with an invalid identifier") {

    it("responds with a 400") {
      withSetup(blogEntries()) {
        get("/blog/this/is/not/valid") { _.status shouldBe 400 }
      }
    }

  }

  describe("a blog entry page") {

    it("responds with a 200") {
      withSetup(blogEntries("first-post" -> "# Hello, World!")) {
        get("/blog/first-post") { _.status shouldBe 200 }
      }
    }

    it("has content-type text/html") {
      withSetup(blogEntries("year-in-review" -> "# tedious blah")) {
        get("/blog/year-in-review") { _.headers("Content-Type") should contain("text/html; charset=UTF-8") }
      }
    }

    it("has the title of the Markdown document in the h1 and title elements") {
      withSetup(blogEntries("titled" -> "# A title!")) {
        get("/blog/titled") { response =>
          val page = new BlogEntryPage(response.body)
          page.title shouldBe "A title!"
          page.h1.text shouldBe "A title!"
        }
      }
    }

    it("has the content of the markdown document as HTML") {
      withBlogEntries("has-content" ->
        """# Content Galore!
          |
          |This is an __amazing__ page of _content_.
          |
          |Don't knock it.
        """.stripMargin) {
        get("/blog/has-content") { response =>
          val page = new BlogEntryPage(response.body)
          page.h1.text shouldBe "Content Galore!"
          page.content.paragraphs.head shouldBe <p>This is an <b>amazing</b> page of <i>content</i>.</p>
          page.content.paragraphs.last shouldBe <p>Don't knock it.</p>
        }
      }
    }

    it("has a copyright notice") {
      withSetup(blogEntries("top-content" -> "# this is well worth copyrighting")) {
        get("/blog/top-content") { response =>
          new BlogEntryPage(response.body).footer.copyrightNotice shouldBe Some("© 2016-2017 Jim Kinsey")
        }
      }
    }

    it("includes the last modified date of the article") {
      withSetup(blogEntries(("2010-10-12T17:05:00Z", "dated", "# Dated!"))) {
        get("/blog/dated") { response =>
          val page = new BlogEntryPage(response.body)
          page should have('date ("Tuesday, 12 October 2010"))
        }
      }
    }

    it("has a navigation for returning to the index page") {
      withSetup(blogEntries("navigable" -> "# Navigable!")) {
        get("/blog/navigable") { response =>
          val page = new BlogEntryPage(response.body)
          page.navigation.items.map(_.url) should contain("/blog/")
        }
      }
    }

  }

}

