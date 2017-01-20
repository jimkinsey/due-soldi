package duesoldi

import duesoldi.pages.BlogEntryPage
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

  describe("getting an invalid blog entry") {

    it("responds with a 500") {
      withBlogEntries("no-title" -> "boom") {
        get("/blog/no-title") { _ .status shouldBe 500 }
      }
    }

  }

  describe("a blog entry page") {

    it("responds with a 200") {
      withBlogEntries("first-post" -> "# Hello, World!") {
        get("/blog/first-post") { _.status shouldBe 200 }
      }
    }

    it("has content-type text/html") {
      withBlogEntries("year-in-review" -> "# tedious blah") {
        get("/blog/year-in-review") { _.headers("Content-Type") should contain("text/html; charset=UTF-8") }
      }
    }

    it("has the title of the Markdown document in the h1 and title elements") {
      withBlogEntries("titled" -> "# A title!") {
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

  }

}

