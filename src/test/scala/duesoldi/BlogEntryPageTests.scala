package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogEntryPage
import duesoldi.storage.BlogStorage._
import duesoldi.storage.Database._
import duesoldi.testapp.ServerRequests._
import duesoldi.testapp.TestApp.runningApp
import utest._

class BlogEntryPageTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "getting a non-existent blog entry" - {
      "responds with a 404" - {
        withSetup(
          database,
          runningApp,
          blogEntries()
        ) { implicit env =>
          for {
            res <- get("/blog/what-i-had-for-breakfast")
          } yield {
            assert(res.status == 404)
          }
        }
      }
    }
    "getting an invalid blog entry" - {
      "responds with a 500" - {
        withSetup(
          database,
          runningApp,
          blogEntries("no-title" -> "boom")
        ) { implicit env =>
          for {
            res <- get("/blog/no-title")
          } yield {
            assert(res.status == 500)
          }
        }
      }
    }
    "getting a blog entry with an invalid identifier" - {
      "responds with a 400" - {
        withSetup(
          database,
          runningApp,
          blogEntries()
        ) { implicit env =>
          for {
            res <- get("""/blog/NOT+A+VALID+ID""")
          } yield {
            assert(res.status == 400)
          }
        }
      }
    }
    "a blog entry page" - {
      "responds with a 200" - {
        withSetup(
          database,
          runningApp,
          blogEntries("first-post" -> "# Hello, World!")
        ) { implicit env =>
          for {
            res <- get("/blog/first-post")
          } yield {
            assert(res.status == 200)
          }
        }
      }
      "has content-type text/html" - {
        withSetup(
          database,
          runningApp,
          blogEntries("year-in-review" -> "# tedious blah")
        ) { implicit env =>
          for {
            res <- get("/blog/year-in-review")
          } yield {
            assert(res.headers("Content-Type") contains "text/html; charset=UTF-8")
          }
        }
      }
      "has the title of the Markdown document in the h1 and title elements" - {
        withSetup(
          database,
          runningApp,
          blogEntries("titled" -> "# A title!")
        ) { implicit env =>
          for {
            res <- get("/blog/titled")
          } yield {
            val page = new BlogEntryPage(res.body)
            assert(
              page.title == "A title!",
              page.h1.text == "A title!"
            )
          }
        }
      }
      "has the content of the markdown document as HTML" - {
        withSetup(
          database,
          runningApp,
          blogEntries("has-content" ->
            """# Content Galore!
            |
            |This is an __amazing__ page of _content_.
            |
            |Don't knock it.
          """.
              stripMargin)
        ) { implicit env =>
          for {
            response <- get("/blog/has-content")
          } yield {
            val page = new BlogEntryPage(response.body)
            assert(
              page.h1.text == "Content Galore!",
              page.content.paragraphs.head == <p>This is an <b>amazing</b> page of <i>content</i>.</p>,
              page.content.paragraphs.last == <p>Don't knock it.</p>
            )
          }
        }
      }
      "may include images" - {
        withSetup(
          database,
          blogEntries("has-image" ->
            """# A cat!
              |
              |![A cat alt-text](/blog/has-image/images/a-cat.gif "A cat title")
            """.stripMargin)
        ) { implicit env =>
          for {
            pageResponse <- get("/blog/has-image")
          } yield {
            val page = new BlogEntryPage(pageResponse.body)
            assert(
              page.content.images.head.src == "/blog/has-image/images/a-cat.gif",
              page.content.images.head.alt == "A cat alt-text",
              page.content.images.head.title contains "A cat title"
            )
          }
        }
      }
      "has a copyright notice" - {
        withSetup(
          database,
          runningApp,
          blogEntries("top-content" -> "# this is well worth copyrighting")
        ) { implicit env =>
          for {
            response <- get("/blog/top-content")
          } yield {
            assert(new BlogEntryPage(response.body).footer.copyrightNotice.contains("© 2016-2017 Jim Kinsey"))
          }
        }
      }
      "includes the last modified date of the article" - {
        withSetup(
          database,
          runningApp,
          blogEntries(("2010-10-12T17:05:00Z", "dated", "# Dated!"))
        ) { implicit env =>
          for {
            response <- get("/blog/dated")
          } yield {
            val page = new BlogEntryPage(response.body)
            assert(page.date == "Tuesday, 12 October 2010")
          }
        }
      }
      "has a navigation for returning to the index page" - {
        withSetup(
          database,
          runningApp,
          blogEntries("navigable" -> "# Navigable!")
        ) { implicit env =>
          for {
            response <- get("/blog/navigable")
          } yield {
            val page = new BlogEntryPage(response.body)
            assert(page.navigation.items.map(_.url).contains("/blog/"))
          }
        }
      }
    }
  }
}
