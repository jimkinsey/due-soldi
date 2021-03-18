package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests.get
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

      "includes the series title" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("series-id").withTitle("Series Title"))
        ) { implicit env =>
          for {
            res <- get("/gallery/series/series-id")
            page = new SeriesPage(res.body.asString)
          } yield {
            assert(
              page.title == "Series Title",
              page.h1.text() == "Series Title"
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
              page.workTitled("One").link == "/gallery/one",
              page.workTitled("One").thumbnailURL == "/images/one-w200.jpg",
              page.workTitled("Two").link == "/gallery/two",
              page.workTitled("Two").thumbnailURL == "/images/two-w200.jpg",
            )
          }
        }
      }

    }

  }
}
