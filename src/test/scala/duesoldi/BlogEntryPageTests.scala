package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogEntryPage
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncFunSpec

class BlogEntryPageTests extends AsyncFunSpec with BlogStorage with Database with ServerSupport with ServerRequests {

  import org.scalatest.Matchers._

  describe("getting a non-existent blog entry") {

    it("responds with a 404") {
      withSetup(
        database,
        blogEntries()) {
        withServer { implicit server =>
          for {
            res <- get("/blog/what-i-had-for-breakfast")
          } yield {
            res.status shouldBe 404
          }
        }
      }
    }

  }

  describe("getting an invalid blog entry") {

    it("responds with a 500") {
      withSetup(
        database,
        blogEntries("no-title" -> "boom")) {
        withServer { implicit server =>
          for {
            res <- get("/blog/no-title")
          } yield {
            res.status shouldBe 500
          }
        }
      }
    }

  }

  describe("getting a blog entry with an invalid identifier") {

    it("responds with a 400") {
      withSetup(database,
        blogEntries()) {
        withServer { implicit server =>
          for {
            res <- get("/blog/this/is/not/valid")
          } yield {
            res.status shouldBe 400
          }
        }
      }
    }

  }

  describe("a blog entry page") {

    it("responds with a 200") {
      withSetup(
        database,
        blogEntries("first-post" -> "# Hello, World!")) {
        withServer { implicit server =>
          for {
            res <- get("/blog/first-post")
          } yield {
            res.status shouldBe 200
          }
        }
      }
    }

    it("has content-type text/html") {
      withSetup(
        database,
        blogEntries("year-in-review" -> "# tedious blah")) {
        withServer { implicit server =>
          for {
            res <- get("/blog/year-in-review")
          } yield {
            res.headers("Content-Type") should contain("text/html; charset=UTF-8")
          }
        }
      }
    }

    it("has the title of the Markdown document in the h1 and title elements") {
      withSetup(
        database,
        blogEntries("titled" -> "# A title!")) {
        withServer { implicit server =>
          for {
            res <- get("/blog/titled")
          } yield {
            val page = new BlogEntryPage(res.body)
            page.title shouldBe "A title!"
            page.h1.text shouldBe "A title!"
          }
        }
      }
    }

    it("has the content of the markdown document as HTML") {
      withSetup(
        database,
        blogEntries("has-content" ->
          """# Content Galore!
            |
            |This is an __amazing__ page of _content_.
            |
            |Don't knock it.
          """.stripMargin)) {
        withServer { implicit server =>
          for {
            response <- get("/blog/has-content")
          } yield {
            val page = new BlogEntryPage(response.body)
            page.h1.text shouldBe "Content Galore!"
            page.content.paragraphs.head shouldBe <p>This is an <b>amazing</b> page of <i>content</i>.</p>
            page.content.paragraphs.last shouldBe <p>Don't knock it.</p>
          }
        }
      }
    }

    it("has a copyright notice") {
      withSetup(
        database,
        blogEntries("top-content" -> "# this is well worth copyrighting")) {
        withServer { implicit server =>
          for {
            response <- get("/blog/top-content")
          } yield {
            new BlogEntryPage(response.body).footer.copyrightNotice shouldBe Some("© 2016-2017 Jim Kinsey")
          }
        }
      }
    }

    it("includes the last modified date of the article") {
      withSetup(
        database,
        blogEntries(("2010-10-12T17:05:00Z", "dated", "# Dated!"))) {
        withServer { implicit server =>
          for {
            response <- get("/blog/dated")
          } yield {
            val page = new BlogEntryPage(response.body)
            page should have('date ("Tuesday, 12 October 2010"))
          }
        }
      }
    }

    it("has a navigation for returning to the index page") {
      withSetup(
        database,
        blogEntries("navigable" -> "# Navigable!")) {
        withServer { implicit server =>
          for {
            response <- get("/blog/navigable")
          } yield {
            val page = new BlogEntryPage(response.body)
            page.navigation.items.map(_.url) should contain("/blog/")
          }
        }
      }
    }
  }
}

