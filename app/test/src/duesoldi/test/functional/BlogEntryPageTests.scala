package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp.runningApp
import hammerspace.testing.CustomMatchers._
import duesoldi.test.support.pages.{BlogEntryPage, OgMetadata}
import duesoldi.test.support.setup.BlogStorage._
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.FeatureSwitching.{featureDisabled, featureEnabled}
import duesoldi.test.support.setup.Setup.withSetup
import hammerspace.testing.StreamHelpers._
import utest._

object BlogEntryPageTests
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
            assert(res.headers("Content-type") contains "text/html; charset=UTF-8")
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
            val page = new BlogEntryPage(res.body.asString)
            assert(
              page.title == "A title!",
              page.h1.text == "A title!"
            )
          }
        }
      }
      "includes the description of the blog entry after the title" - {
        withSetup(
          database,
          runningApp,
          blogEntries(
            entry.withId("described").withDescription("A test document for testing descriptions")
          )
        ) { implicit env =>
          for {
            res <- get("/blog/described")
            page = new BlogEntryPage(res.body.asString)
          } yield {
            assert(
              page.description contains "A test document for testing descriptions"
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
            val page = new BlogEntryPage(response.body.asString)
            assert(
              page.h1.text == "Content Galore!",
              page.content.paragraphs.head == "<p>This is an <b>amazing</b> page of <i>content</i>.</p>",
              page.content.paragraphs.last == "<p>Don't knock it.</p>"
            )
          }
        }
      }
      "may include images" - {
        withSetup(
          database,
          runningApp,
          blogEntries("has-image" ->
            """# A cat!
              |
              |![A cat alt-text](/blog/has-image/images/a-cat.gif "A cat title")
            """.stripMargin)
        ) { implicit env =>
          for {
            pageResponse <- get("/blog/has-image")
          } yield {
            val page = new BlogEntryPage(pageResponse.body.asString)
            assert(
              page.content.images.head.src == "/blog/has-image/images/a-cat.gif",
              page.content.images.head.alt == "A cat alt-text",
              page.content.images.head.title contains "A cat title"
            )
          }
        }
      }
      "may inlude inline HTML blocks" - {
        withSetup(
          database,
          runningApp,
          blogEntries("has-html" ->
            """# HTML
              |
              |<p>
              |hello
              |</p>
            """.stripMargin)
        ) { implicit env =>
          for {
            pageResponse <- get("/blog/has-html")
            paragraphs = new BlogEntryPage(pageResponse.body.asString).content.paragraphs
          } yield {
            assert(paragraphs contains "<p> hello </p>")
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
            footer = new BlogEntryPage(response.body.asString).footer
          } yield {
            assert(footer.copyrightNotice.exists(_ matches """© \d\d\d\d-\d\d\d\d Jim Kinsey"""))
          }
        }
      }
      "includes the last modified date of the article" - {
        withSetup(
          database,
          runningApp,
          blogEntries("dated" -> "# Dated!")
        ) { implicit env =>
          for {
            response <- get("/blog/dated")
            date = new BlogEntryPage(response.body.asString).date
          } yield {
            assert(date hasDateFormat "EEEE, dd MMMM yyyy")
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
            val page = new BlogEntryPage(response.body.asString)
            assert(page.navigation.items.map(_.url).contains("/blog/"))
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
            response <- get("/blog/title")
            page = new BlogEntryPage(response.body.asString)
            cssResponse <- get(page.cssUrl)
          } yield {
            assert(cssResponse.status == 200)
          }
        }
      }
      "has a Twitter Card" - {
        withSetup(
          database,
          runningApp,
          blogEntry("title" ->
            """# Title
              |
              |The start of the content
            """.stripMargin
          ),
          featureEnabled("TWITTER_CARDS")
        ) { implicit env =>
          for {
            response <- get("/blog/title")
            page = new BlogEntryPage(response.body.asString)
          } yield {
            assert(page.twitterMetadata.isDefined)
            page.twitterMetadata.foreach { data =>
              assert(
                data.card == "summary"
              )
            }
          }
        }
      }
      "has no Twitter Card when the feature is disabled" - {
        withSetup(
          database,
          runningApp,
          blogEntry("title" -> "# Title"),
          featureDisabled("TWITTER_CARDS")
        ) { implicit env =>
          for {
            response <- get("/blog/title")
            twitterCard = new BlogEntryPage(response.body.asString).twitterMetadata
          } yield {
            assert(twitterCard.isEmpty)
          }
        }
      }
      "has some Open Graph metadata" - {
        withSetup(
          database,
          runningApp,
          blogEntry(
            entry.withId("title")
              .withDescription("Some excellent content.")
              .withContent(
                """# Title
                  |
                  |The start of the content
                """.stripMargin)
          )
        ) { implicit env =>
          for {
            response <- get("/blog/title")
            page = new BlogEntryPage(response.body.asString)
          } yield {
            assertMatch(page.ogMetadata) {
              case Some(OgMetadata("Title", Some("Some excellent content."), _)) => {}
            }
          }
        }
      }
      "includes the first image, if any, in the Open Graph metadata" - {
        withSetup(
          database,
          runningApp,
          blogEntry("title" ->
            """# Title
              |
              |The start of the content
              |
              |![The first image](/blog/title/images/image.gif "An image title")
            """.stripMargin
          )
        ) { implicit env =>
          for {
            response <- get("/blog/title")
            page = new BlogEntryPage(response.body.asString)
          } yield {
            assertMatch(page.ogMetadata.flatMap(_.image)) {
              case Some(OgMetadata.Image("/blog/title/images/image.gif", Some("The first image"))) => {}
            }
          }
        }
      }
    }
  }
}

