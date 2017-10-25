package duesoldi

import java.time.format.DateTimeFormatter

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogIndexPage
import duesoldi.storage.BlogStorage._
import duesoldi.storage.Database._
import duesoldi.test.matchers.CustomMatchers._
import duesoldi.testapp.ServerRequests._
import duesoldi.testapp.TestApp.runningApp
import utest._

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
            response <- get("/blog")
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
            assert(response.headers("Content-Type").contains("text/html; charset=UTF-8"))
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
          } yield {
            assert(new BlogIndexPage(response.body).footer.copyrightNotice.contains("© 2016-2017 Jim Kinsey"))
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
            val page: BlogIndexPage = new BlogIndexPage(response.body)
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
            val page = new BlogIndexPage(response.body)
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
            date = new BlogIndexPage(response.body).blogEntries.head.date
          } yield {
            assert(date hasDateFormat "EEEE, dd MMMM yyyy")
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
            val page = new BlogIndexPage(response.body)
            assert(
              page.blurb.title == "About",
              page.blurb.paragraphs.size() > 0
            )
          }
        }
      }
    }
  }
}

