package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.pages.BlogIndexPage
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import utest._

object BlogIndexPageTests
  extends TestSuite 
  with BlogStorage 
  with Database 
  with ServerRequests 
  with ServerSupport 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "visiting 'blog' without the trailing slash" - {
      "redirects to the version with the trailing slash" - {
        withSetup(database, blogEntries("id" -> "# Content!")) {
          withServer { implicit server =>
            for {
              response <- get("/blog")
            } yield {
              assert(
                response.status == 301,
                response.headers("Location") == List(s"http://${server.host}:${server.port}/blog/")
              )
            }
          }
        }
      }
    }
    "when there are no blog entries" - {
      "responds with a 404" - {
        withSetup(database, blogEntries()) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              assert(response.status == 404)
            }
          }
        }
      }
    }
    "when there are invalid blog entries" - {
      "filters out the invalid entries" - {
        withSetup(database, blogEntries(
          "invalid-content" -> "boom",
          "InVALid ID" -> "# Boom",
          "valid-content-and-id" -> "# Hello!")) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              lazy val page = new BlogIndexPage(response.body)
              assert(
                response.status == 200,
                page.blogEntries.size == 1,
                page.blogEntries.head.link == "/blog/valid-content-and-id",
                page.blogEntries.head.title == "Hello!"
              )
            }
          }
        }
      }
    }
    "the blog index page" - {
      "responds with a 200" - {
        withSetup(database, blogEntries("first-post" -> "# Hello, World!")) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              assert(response.status == 200)
            }
          }
        }
      }
      "has content-type text/html" - {
        withSetup(database, blogEntries("year-in-review" -> "# tedious blah")) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              assert(response.headers("Content-Type").contains("text/html; charset=UTF-8"))
            }
          }
        }
      }
      "has a copyright notice" - {
        withSetup(database, blogEntries("top-content" -> "# this is well worth copyrighting")) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              assert(new BlogIndexPage(response.body).footer.copyrightNotice.contains("© 2016-2017 Jim Kinsey"))
            }
          }
        }
      }
      "has a title and heading" - {
        withSetup(database, blogEntries("content" -> "# _Content_, mofos")) {
          withServer { implicit server =>
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
      }
      "lists the entries in reverse order of last modification" - {
        withSetup(database, blogEntries(
          ("2010-10-12T17:05:00Z", "first-post", "# First"),
          ("2012-12-03T09:34:00Z", "tricky-second-post", "# Second"),
          ("2016-04-01T09:45:00Z", "sorry-for-lack-of-updates", "# Third"))) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              val page = new BlogIndexPage(response.body)
              assert(page.blogEntries.map(_.title) == Seq("Third", "Second", "First"))
            }
          }
        }
      }
      "includes the last modified date in the entry" - {
        withSetup(database, blogEntries(("2010-10-12T17:05:00Z", "dated", "# Dated!"))) {
          withServer { implicit server =>
            for {
              response <- get("/blog/")
            } yield {
              val page = new BlogIndexPage(response.body)
              assert(page.blogEntries.head.date == "Tuesday, 12 October 2010")
            }
          }
        }
      }
      "has a bio section with a title and some text" - {
        withSetup(database, blogEntries("year-in-review" -> "# Year in review!")) {
          withServer { implicit server =>
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
}

