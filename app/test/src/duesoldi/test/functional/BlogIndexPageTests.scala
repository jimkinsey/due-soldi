package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp.runningApp
import hammerspace.testing.CustomMatchers._
import duesoldi.test.support.pages.BlogIndexPage
import duesoldi.test.support.setup.BlogStorage._
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.Setup.withSetup
import utest._
import hammerspace.testing.StreamHelpers._

object BlogIndexPageTests
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "visiting 'blog' without the trailing slash" - {
      "redirects to the version with the trailing slash" - {
        withSetup(
          database,
          runningApp,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            response <- getNoFollow("/blog")
          } yield {
            assert(
              response.status == 301,
              response.headers("Location").head.endsWith("/blog/")
            )
          }
        }
      }
    }
    "when there are no blog entries" - {
      "responds with a 404" - {
        withSetup(
          database,
          runningApp,
          blogEntries()
        ) { implicit env =>
          for {
            response <- get("/blog/")
          } yield {
            assert(response.status == 404)
          }
        }
      }
    }
    "the blog index page" - {
      "responds with a 200" - {
        withSetup(
          database,
          runningApp,
          blogEntries("first-post" -> "# Hello, World!")
        ) { implicit env =>
          for {
            response <- get("/blog/")
          } yield {
            assert(response.status == 200)
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
            response <- get("/blog/")
          } yield {
            assert(response.headers("Content-type").contains("text/html; charset=UTF-8"))
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
            response <- get("/blog/")
            footer = new BlogIndexPage(response.body.asString).footer
          } yield {
            assert(footer.copyrightNotice.exists(_ matches """© \d\d\d\d-\d\d\d\d Jim Kinsey"""))
          }
        }
      }
      "has a title and heading" - {
        withSetup(
          database,
          runningApp,
          blogEntries("content" -> "# _Content_, mofos")
        ) { implicit env =>
          for {
            response <- get("/blog/")
          } yield {
            val page: BlogIndexPage = new BlogIndexPage(response.body.asString)
            assert(
              page.title == "Jim Kinsey's Blog",
              page.heading == "Latest Blog Entries"
            )
          }
        }
      }
      "lists the entries in reverse order of last modification" - {
        withSetup(
          database,
          runningApp,
          blogEntry("first-post" -> "# First"),
          blogEntry("tricky-second-post" -> "# Second"),
          blogEntry("sorry-for-lack-of-updates" -> "# Third")
        ) { implicit env =>
          for {
            response <- get("/blog/")
          } yield {
            val page = new BlogIndexPage(response.body.asString)
            assert(page.blogEntries.map(_.title) == Seq("Third", "Second", "First"))
          }
        }
      }
      "includes the last modified date in the entry" - {
        withSetup(
          database,
          runningApp,
          blogEntry("dated" -> "# Dated!")
        ) { implicit env =>
          for {
            response <- get("/blog/")
            date = new BlogIndexPage(response.body.asString).blogEntries.head.date
          } yield {
            assert(date hasDateFormat "EEEE, dd MMMM yyyy")
          }
        }
      }
      "includes the blog entry description in the entry" - {
        withSetup(
          database,
          runningApp,
          blogEntry(entry withDescription "Groundbreaking #content")
        ) { implicit env =>
          for {
            response <- get("/blog/")
            entry = new BlogIndexPage(response.body.asString).blogEntries.head
          } yield {
            assert(entry.description == "Groundbreaking #content")
          }
        }
      }
      "has a bio section with a title and some text" - {
        withSetup(
          database,
          runningApp,
          blogEntries("year-in-review" -> "# Year in review!")
        ) { implicit env =>
          for {
            response <- get("/blog/")
          } yield {
            val page = new BlogIndexPage(response.body.asString)
            assert(
              page.blurb.title == "About",
              page.blurb.paragraphs.size() > 0
            )
          }
        }
      }
      "has a linked CSS file to provide styling" - {
        withSetup(
          database,
          runningApp,
          blogEntries("title" -> "# Title")
        ) { implicit env =>
          for {
            response <- get("/blog/")
            page = new BlogIndexPage(response.body.asString)
            cssResponse <- get(page.cssUrl)
          } yield {
            assert(
              cssResponse.status == 200
            )
          }
        }
      }
    }
  }
}

