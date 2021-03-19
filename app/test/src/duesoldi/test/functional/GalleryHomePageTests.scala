package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.pages.{ArtworkPage, GalleryHomePage, OgMetadata}
import duesoldi.test.support.setup.ConfigOverride.configOverride
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.GalleryStorage._
import duesoldi.test.support.setup.Setup.withSetup
import hammerspace.testing.StreamHelpers._
import utest._

object GalleryHomePageTests
extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {

    "the gallery home page" - {

      "responds with a 404 when there are no artworks" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            res <- get("/gallery")
          } yield {
            assert(res.status == 404)
          }
        }
      }

      "responds with a 200 when there are artworks" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork)
        ) { implicit env =>
          for {
            res <- get("/gallery")
          } yield {
            assert(
              res.status == 200,
              res.headers("Content-type") contains "text/html; charset=UTF-8"
            )
          }
        }
      }

      "includes a link to every artwork" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("one"),
            artwork.withId("two"),
            artwork.withId("three"),
          )
        ) { implicit env =>
          for {
            res <- get("/gallery")
            page = new GalleryHomePage(res.body.asString)
          } yield {
            assert(
              page.artworkLinks contains "/gallery/artwork/one",
              page.artworkLinks contains "/gallery/artwork/two",
              page.artworkLinks contains "/gallery/artwork/three",
            )
          }
        }
      }

      "includes a thumbnail for every artwork" - {
        withSetup(
          database,
          configOverride("IMAGE_BASE_URL" -> "https://images"),
          runningApp,
          artworks(
            artwork.withId("one").withImageURL("/image/one-main.jpg"),
            artwork.withId("two").withImageURL("/image/two-main.jpg")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery")
            page = new GalleryHomePage(res.body.asString)
          } yield {
            assert(
              page.artworkThumbnailURLs contains "https://images/image/one-main-w200.jpg",
              page.artworkThumbnailURLs contains "https://images/image/one-main-w200.jpg"
            )
          }
        }
      }

      "groups the artworks by series, placing any not in a series into a misc section" - {
        withSetup(
          database,
          runningApp,
          series(
            series withId "series-one" withTitle "Series One",
            series withId "series-two" withTitle "Series Two"
          ),
          artworks(
            artwork withId "one" withTitle "One" belongingToSeries "series-one",
            artwork withId "two" withTitle "Two" belongingToSeries "series-one",
            artwork withId "three" withTitle "Three" belongingToSeries "series-two",
            artwork withId "four" withTitle "Four"
          )
        ) { implicit env =>
          for {
            res <- get("/gallery")
            page = new GalleryHomePage(res.body.asString)
            seriesOne = page.seriesNamed("Series One")
            seriesTwo = page.seriesNamed("Series Two")
            misc = page.seriesNamed("Miscellaneous works")

            seriesOneTitles = seriesOne.map(_.works.map(_.title))
          } yield {
            assert(
              seriesOneTitles contains Seq("One", "Two"),
              seriesTwo exists (_.works.map(_.title) == Seq("Three")),
              misc exists (_.works.map(_.title) == Seq("Four"))
            )
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
            response <- get("/gallery")
            footer = new GalleryHomePage(response.body.asString).footer
          } yield {
            assert(footer.copyrightNotice.exists(_ matches """© \d\d\d\d-\d\d\d\d Jim Kinsey"""))
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

      "has a linked CSS file to provide styling" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork)
        ) { implicit env =>
          for {
            response <- get("/gallery")
            page = new GalleryHomePage(response.body.asString)
            cssResponse <- get(page.cssUrl)
          } yield {
            assert(cssResponse.status == 200)
          }
        }
      }

      "has some Open Graph metadata" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork
          )
        ) { implicit env =>
          for {
            response <- get("/gallery")
            page = new GalleryHomePage(response.body.asString)
          } yield {
            assert(
              page.ogMetadata.get.title == "Jim Kinsey's Online Gallery"
            )
          }
        }
      }
    }
  }
}

