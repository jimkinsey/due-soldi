package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.pages.{ArtworkPage, OgMetadata}
import duesoldi.test.support.setup.GalleryStorage._
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.Setup.withSetup
import hammerspace.testing.CustomMatchers._
import hammerspace.testing.StreamHelpers._
import utest._

object ArtworkPageTests
extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "getting a non-existent artwork" - {
      "responds with a 404" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
          } yield {
            assert(res.status == 404)
          }
        }
      }
    }
    "getting an artwork with an invalid identifier" - {
      "responds with a 400" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            res <- get("""/gallery/NOT+A+VALID+ID""")
          } yield {
            assert(res.status == 400)
          }
        }
      }
    }
    "an artwork page" - {
      "responds with a 200" - {
        withSetup(
          database,
          runningApp,
          artworks("untitled-masterpiece" -> "Untitled Masterpiece")
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
          } yield {
            assert(res.status == 200)
          }
        }
      }
      "has content-type text/html" - {
        withSetup(
          database,
          runningApp,
          artworks("untitled-masterpiece" -> "Untitled Masterpiece")
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
          } yield {
            assert(res.headers("Content-type") contains "text/html; charset=UTF-8")
          }
        }
      }
      "includes the title, timeframe and materials" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("untitled-masterpiece")
              .withTitle("Untitled Masterpiece")
              .withTimeframe("early 2021")
              .withMaterials("gouache on paper")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
            page = new ArtworkPage(res.body.asString)
          } yield {
            assert(page.title == "Untitled Masterpiece")
            assert(page.h1.text() == "Untitled Masterpiece")
            assert(page.timeframe contains "early 2021")
            assert(page.materials contains "gouache on paper")
          }
        }
      }
      "includes the description rendered as HTML" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("untitled-masterpiece")
              .withDescription("""It is _pure_ genius""")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
            page = new ArtworkPage(res.body.asString)
          } yield {
            assert(page.description contains """<p>It is <i>pure</i> genius</p>""")
          }
        }
      }
      "includes the URL to the artwork image" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("untitled-masterpiece")
              .withImageURL("/path/to/image.png")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery/untitled-masterpiece")
            page = new ArtworkPage(res.body.asString)
          } yield {
            assert(page.artworkImageURL == "/path/to/image.png")
          }
        }
      }
      "has a copyright notice" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("untitled-masterpiece")
          )
        ) { implicit env =>
          for {
            response <- get("/gallery/untitled-masterpiece")
            footer = new ArtworkPage(response.body.asString).footer
          } yield {
            assert(footer.copyrightNotice.exists(_ matches """© \d\d\d\d-\d\d\d\d Jim Kinsey"""))
          }
        }
      }
    }

//      "has a navigation for returning to the index page" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntries("navigable" -> "# Navigable!")
//        ) { implicit env =>
//          for {
//            response <- get("/blog/navigable")
//          } yield {
//            val page = new BlogEntryPage(response.body.asString)
//            assert(page.navigation.items.map(_.url).contains("/blog/"))
//          }
//        }
//      }
//      "has a linked CSS file to provide styling" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntries("title" -> "# Title")
//        ) { implicit env =>
//          for {
//            response <- get("/blog/title")
//            page = new BlogEntryPage(response.body.asString)
//            cssResponse <- get(page.cssUrl)
//          } yield {
//            assert(cssResponse.status == 200)
//          }
//        }
//      }
//      "has a Twitter Card" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntry("title" ->
//            """# Title
//              |
//              |The start of the content
//            """.stripMargin
//          ),
//          featureEnabled("TWITTER_CARDS")
//        ) { implicit env =>
//          for {
//            response <- get("/blog/title")
//            page = new BlogEntryPage(response.body.asString)
//          } yield {
//            assert(page.twitterMetadata.isDefined)
//            page.twitterMetadata.foreach { data =>
//              assert(
//                data.card == "summary"
//              )
//            }
//          }
//        }
//      }
//      "has no Twitter Card when the feature is disabled" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntry("title" -> "# Title"),
//          featureDisabled("TWITTER_CARDS")
//        ) { implicit env =>
//          for {
//            response <- get("/blog/title")
//            twitterCard = new BlogEntryPage(response.body.asString).twitterMetadata
//          } yield {
//            assert(twitterCard.isEmpty)
//          }
//        }
//      }
//      "has some Open Graph metadata" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntry(
//            entry.withId("title")
//              .withDescription("Some excellent content.")
//              .withContent(
//                """# Title
//                  |
//                  |The start of the content
//                """.stripMargin)
//          )
//        ) { implicit env =>
//          for {
//            response <- get("/blog/title")
//            page = new BlogEntryPage(response.body.asString)
//          } yield {
//            assertMatch(page.ogMetadata) {
//              case Some(OgMetadata("Title", Some("Some excellent content."), _)) => {}
//            }
//          }
//        }
//      }
//      "includes the first image, if any, in the Open Graph metadata" - {
//        withSetup(
//          database,
//          runningApp,
//          blogEntry("title" ->
//            """# Title
//              |
//              |The start of the content
//              |
//              |![The first image](/blog/title/images/image.gif "An image title")
//            """.stripMargin
//          )
//        ) { implicit env =>
//          for {
//            response <- get("/blog/title")
//            page = new BlogEntryPage(response.body.asString)
//          } yield {
//            assertMatch(page.ogMetadata.flatMap(_.image)) {
//              case Some(OgMetadata.Image("/blog/title/images/image.gif", Some("The first image"))) => {}
//            }
//          }
//        }
//      }
//    }
  }
}

