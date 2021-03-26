package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests.{get, getNoFollow}
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.pages.SeriesPage
import duesoldi.test.support.setup.Database.database
import duesoldi.test.support.setup.GalleryStorage.{artwork, artworks, series}
import duesoldi.test.support.setup.Setup.withSetup
import hammerspace.testing.StreamHelpers.ByteStreamHelper
import utest._

object SeriesPageTests
extends TestSuite {
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "the /series/ path" - {

      "redirects to the gallery home page" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            res <- getNoFollow("/series/")
          } yield {
            assert(
              res.status == 301,
              res.headers("Location").head == "/gallery/"
            )
          }
        }
      }

    }

    "a series page" - {

      "responds with a 404 for a non-existent series" - {
        withSetup(
          database,
          runningApp,
          series()
        ) { implicit env =>
          for {
            res <- get("/gallery/series/doesnt-exist")
          } yield {
            assert(res.status == 404)
          }
        }
      }

      "responds with an HTML page with a 200 response code" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("series-id"))
        ) { implicit env =>
          for {
            res <- get("/gallery/series/series-id")
          } yield {
            assert(
              res.status == 200,
              res.headers("Content-type") contains "text/html; charset=UTF-8"
            )
          }
        }
      }

      "includes the series title and description" - {
        withSetup(
          database,
          runningApp,
          series(
            series.withId("series-id")
              .withTitle("Series Title")
              .withDescription("A _lovely_ series of **wonderful** works")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery/series/series-id")
            page = new SeriesPage(res.body.asString)
          } yield {
            assert(
              page.title == "Series Title",
              page.h1.text() == "Series Title",
              page.descriptionHTML == "<p>A <i>lovely</i> series of <b>wonderful</b> works</p>"
            )
          }
        }
      }

      "includes a linked thumbnail for every image in the series" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("series-id").withTitle("Series Title")),
          artworks(
            artwork.withId("one").withTitle("One").withImageURL("/images/one.jpg").belongingToSeries("series-id"),
            artwork.withId("two").withTitle("Two").withImageURL("/images/two.jpg").belongingToSeries("series-id")
          )
        ) { implicit env =>
          for {
            res <- get("/gallery/series/series-id")
            page = new SeriesPage(res.body.asString)
          } yield {
            assert(
              page.workTitled("One").link == "/gallery/artwork/one",
              page.workTitled("One").thumbnailURL == "/images/one-w200.jpg",
              page.workTitled("Two").link == "/gallery/artwork/two",
              page.workTitled("Two").thumbnailURL == "/images/two-w200.jpg",
            )
          }
        }
      }

      "has a copyright notice" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("a-series")),
          artworks(artwork)
        ) { implicit env =>
          for {
            response <- get("/gallery/series/a-series")
            footer = new SeriesPage(response.body.asString).footer
          } yield {
            assert(footer.copyrightNotice.exists(_ matches """© \d\d\d\d-\d\d\d\d Jim Kinsey"""))
          }
        }
      }

      "has a linked CSS file to provide styling" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("a-series")),
          artworks(artwork)
        ) { implicit env =>
          for {
            response <- get("/gallery/series/a-series")
            page = new SeriesPage(response.body.asString)
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
          series(series.withId("a-series").withTitle("A series")),
          artworks(artwork)
        ) { implicit env =>
          for {
            response <- get("/gallery/series/a-series")
            page = new SeriesPage(response.body.asString)
          } yield {
            assert(
              page.ogMetadata.get.title == "A series"
            )
          }
        }
      }

    }

  }
}
