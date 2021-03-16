package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.pages.{ArtworkEditingPage, ArtworkPage}
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.GalleryStorage._
import duesoldi.test.support.setup.Setup.withSetup
import hammerspace.testing.StreamHelpers._
import ratatoskr.Method._
import ratatoskr.RequestBuilding._
import ratatoskr.ResponseAccess._
import ratatoskr.{Cookie, Method}
import utest._

object ArtworkEditingTests
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "making a PUT request with a YAML document" - {
      "creates an artwork at the specified ID where none already exists" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/artwork/new-work",
              body =
                s"""id: new-work
                   |title: New Work
                   |image-url: /path/to/image.png
                   |description: |
                   |    Description!
                   |""".stripMargin,
              headers = TestApp.adminAuth, "Content-Type" -> "text/x-yaml; charset=utf-8")
            entryResponse <- get("/gallery/new-work")
            page = new ArtworkPage(entryResponse.body.asString)
          } yield {
            assert(
              createResponse.status == 201,
              entryResponse.status == 200,
              page.title == "New Work",
              page.description contains "<p>Description!</p>"
            )
          }
        }
      }
      "returns a bad request response when the id is invalid" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/artwork/';+DROP+TABLE+blog_entry;", body = "# Attack!", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "does not allow creation where no credentials are supplied" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/artwork/new-work", body = "")
          } yield {
            assert(createResponse.status == 401)
          }
        }
      }
      "does not allow creation where the wrong credentials are supplied" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/artwork/new-work",
              body = "# Doesn't matter!",
              headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            assert(createResponse.status == 403)
          }
        }
      }
      "does not allow creation where the artwork already exists" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("already-there"))
        ) { implicit env =>
          for {
            createResponse <- put("/admin/artwork/already-there",
              body = artwork.withId("already-there").toYaml,
              headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 409)
          }
        }
      }
    }
    "making a DELETE request to an artwork URL" - {
      "deletes the blog entry at the specified ID where it already exists" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("existing"))
        ) { implicit env =>
          for {
            deleteResponse <- delete("/admin/artwork/existing", headers = TestApp.adminAuth)
            artworkResponse <- get("/gallery/existing")
          } yield {
            assert(deleteResponse.status == 204)
            assert(artworkResponse.status == 404)
          }
        }
      }
      "returns a 400 when the id is invalid" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- delete("/admin/artwork/';+DROP+TABLE+blog_entry;", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "requires admin priviliges" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("existing"))
        ) { implicit env =>
          for {
            createResponse <- delete("/admin/artwork/existing", headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            assert(createResponse.status == 403)
          }
        }
      }
    }
    "making a GET request to the blog collection" - {
      "returns an empty response when there are no artworks" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            getAllResponse <- get("/admin/artwork", headers = TestApp.adminAuth)
          } yield {
            assert(getAllResponse.body isEmpty)
          }
        }
      }
      "returns a YAML doc listing all artworks when there are some" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("one"), artwork.withId("two"))
        ) { implicit env =>
          for {
            getAllResponse <- get("/admin/artwork", headers = TestApp.adminAuth)
          } yield {
            assert(
              getAllResponse.status == 200,
              getAllResponse.body.asString contains "  id: one",
              getAllResponse.body.asString contains "  id: two"
            )
          }
        }
      }
    }
    "making a DELETE request to the artwork collection" - {
      "deletes all entries" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("one"), artwork.withId("two"))
        ) { implicit env =>
          for {
            deleteAllResponse <- delete("/admin/artwork", headers = TestApp.adminAuth)
            getAllResponse <- get("/admin/artwork", headers = TestApp.adminAuth)
          } yield {
            assert(
              deleteAllResponse.status == 204,
              getAllResponse.body isEmpty
            )
          }
        }
      }
    }
    "making a PUT request to the artwork collection" - {
      "puts all entries" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("1"), artwork.withId("2"), artwork.withId("3"))
        ) { implicit env =>
          for {
            getAllResponse <- get("/admin/artwork", headers = TestApp.adminAuth)
            _ <- delete("/admin/artwork", headers = TestApp.adminAuth)
            putAllResponse <- put("/admin/artwork", body = getAllResponse.body.asString, headers = TestApp.adminAuth)
            getFirstResponse <- get("/admin/artwork/1", headers = TestApp.adminAuth)
            getSecondResponse <- get("/admin/artwork/2", headers = TestApp.adminAuth)
            getThirdResponse <- get("/admin/artwork/3", headers = TestApp.adminAuth)
          } yield {
            assert(
              putAllResponse.status == 201,
              getFirstResponse.status == 200,
              getSecondResponse.status == 200,
              getThirdResponse.status == 200
            )
          }
        }
      }
    }
    "making a GET request to an artwork URL" - {
      "returns a 404 when the artwork does not exist" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            getResponse <- get("/admin/artwork/does-not-exist", headers = TestApp.adminAuth)
          } yield {
            assert(getResponse.status == 404)
          }
        }
      }
      "returns a 400 when the id is invalid" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- get("/admin/artwork/';+DROP+TABLE+blog_entry;", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "returns the artwork YAML doc when it does exist" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("existing"))
        ) { implicit env =>
          for {
            getResponse <- get("/admin/artwork/existing", headers = TestApp.adminAuth)
          } yield {
            assert(
              getResponse.status == 200,
              getResponse.body.asString contains "id: existing"
            )
          }
        }
      }
      "returns an artwork YAML doc which is compatible with the PUT endpoint" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("id"))
        ) { implicit env =>
          for {
            getResponse <- get("/admin/artwork/id", headers = TestApp.adminAuth)
            _ <- delete("/admin/artwork/id", headers = TestApp.adminAuth)
            putResponse <- put("/admin/artwork/id", getResponse.body.asString, headers = TestApp.adminAuth)
          } yield {
            assert(
              putResponse.status == 201
            )
          }
        }
      }
      "requires admin privileges" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            getResponse <- get("/admin/artwork/does-not-exist", headers = BasicAuthorization("not-admin", "password"))
          } yield {
            assert(getResponse.status == 403)
          }
        }
      }
    }
    "The artwork creation page" - {
      "requires authorization by basic auth or cookie" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            noCredentialsOrCookie <- get("/admin/artwork/edit")
            badCredentials <- get("/admin/artwork/edit", headers = BasicAuthorization("not-admin", "password"))
            goodCredentials <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            goodCookie <- get("/admin/artwork/edit", headers = goodCredentials.cookie("adminSessionId").get.toRequestHeader)
            badCookie <- get("/admin/artwork/edit", headers = Cookie("adminSessionId", "").toRequestHeader)
          } yield {
            assert(
              noCredentialsOrCookie.status == 401,
              badCredentials.status == 403,
              goodCredentials.status == 200,
              goodCookie.status == 200,
              badCookie.status == 401
            )
          }
        }
      }
      "uses UTF-8 for encoding" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            editingPage <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            form = new ArtworkEditingPage(editingPage.body.asString).form
          } yield {
            assert(
              editingPage.headers("Content-type") contains "text/html; charset=UTF-8",
              form.acceptCharset == "utf-8"
            )
          }
        }
      }
      "can successfully create an artwork" - {
        withSetup(
          database,
          runningApp,
          artworks()
        ) { implicit env =>
          for {
            editingPage <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            _ = assert(editingPage.status == 200)
            form = new ArtworkEditingPage(editingPage.body.asString).form
            formValues = form
              .id("new-artwork")
              .title("New Artwork")
              .timeframe("early 2021")
              .materials("wax crayon on paper")
              .imageURL("/path/to/image.png")
              .description("A _new_ artwork")
              .values
            submission <- send(POST(form.action).formValues(formValues).cookie(editingPage.cookie("adminSessionId").get))
            newWork <- get("/gallery/new-artwork")
            artworkPage = new ArtworkPage(newWork.body.asString)
          } yield {
            assert(
              submission.status == 201,
              newWork.status == 200,
              artworkPage.title == "New Artwork",
              artworkPage.artworkImageURL == "/path/to/image.png",
              artworkPage.timeframe contains "early 2021",
              artworkPage.materials contains "wax crayon on paper",
              artworkPage.description contains ("<p>A <i>new</i> artwork</p>")
            )
          }
        }
      }
      "provides a list of artworks to edit" - {
        withSetup(
          database,
          runningApp,
          artworks(
            artwork.withId("one"),
            artwork.withId("two"),
          )
        ) { implicit env =>
          for {
            editingPage <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            form = new ArtworkEditingPage(editingPage.body.asString).artworkSelectForm
          } yield {
            assert(
              form.entries == Seq("one", "two")
            )
          }
        }
      }
      "allows an existing artwork to be edited" - {
        withSetup(
          database,
          runningApp,
          artworks(artwork.withId("one").withTitle("ONE"))
        ) { implicit env =>
          for {
            editingPage <- send(
              GET("/admin/artwork/edit")
                .header(TestApp.adminAuth))

            selectForm = new ArtworkEditingPage(editingPage.body.asString).artworkSelectForm
            selectFormValues = selectForm.entry("one").values
            selectEntry <- send(
              Method(selectForm.method)(selectForm.action)
                .query(selectFormValues)
                .cookie(editingPage.cookie("adminSessionId").get))

            editForm = new ArtworkEditingPage(selectEntry.body.asString).form

            editFormValues = editForm
              .title("One")
              .values

            updateEntry <- send(
              POST(editForm.action)
                .formValues(editFormValues)
                .cookie(editingPage.cookie("adminSessionId").get))
            _ = assert(updateEntry.status == 201)

            updatedArtwork <- send(
              GET("/gallery/one"))
            updatedArtworkPage = new ArtworkPage(updatedArtwork.body.asString)
          } yield {
            assert(
              updatedArtworkPage.title == "One"
            )
          }
        }
      }
      "allows a series to be selected from a dropdown, when they exist" - {
        withSetup(
          database,
          runningApp,
          series(series.withId("doodles").withTitle("Doodles")),
          artworks()
        ) { implicit env =>
          for {
            editingPage <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            _ = assert(editingPage.status == 200)
            form = new ArtworkEditingPage(editingPage.body.asString).form
            formValues = form
              .id("new-artwork")
              .title("New Artwork")
              .timeframe("early 2021")
              .materials("wax crayon on paper")
              .imageURL("/path/to/image.png")
              .description("A _new_ artwork")
              .seriesID(form.availableSeries.find(_.title == "Doodles").map(_.id).getOrElse(""))
              .values
            submission <- send(POST(form.action).formValues(formValues).cookie(editingPage.cookie("adminSessionId").get))
            newWork <- get("/gallery/new-artwork")
            artworkPage = new ArtworkPage(newWork.body.asString)
          } yield {
            assert(
              submission.status == 201,
              newWork.status == 200,
              artworkPage.title == "New Artwork",
              artworkPage.artworkImageURL == "/path/to/image.png",
              artworkPage.timeframe contains "early 2021",
              artworkPage.materials contains "wax crayon on paper",
              artworkPage.description contains ("<p>A <i>new</i> artwork</p>"),
              artworkPage.seriesTitle contains "Doodles"
            )
          }
        }
      }
      "allows a new series to be created and associated with the artwork" - {
        withSetup(
          database,
          runningApp,
          series(),
          artworks()
        ) { implicit env =>
          for {
            editingPage <- get("/admin/artwork/edit", headers = TestApp.adminAuth)
            _ = assert(editingPage.status == 200)
            form = new ArtworkEditingPage(editingPage.body.asString).form
            formValues = form
              .id("new-artwork")
              .title("New Artwork")
              .imageURL("/path/to/image.png")
              .newSeriesID("new-series")
              .newSeriesTitle("New series")
              .values
            submission <- send(POST(form.action).formValues(formValues).cookie(editingPage.cookie("adminSessionId").get))
            newWork <- get("/gallery/new-artwork")
            artworkPage = new ArtworkPage(newWork.body.asString)
          } yield {
            assert(
              submission.status == 201,
              newWork.status == 200,
              artworkPage.seriesTitle contains "New series"
            )
          }
        }
      }

    }
  }
}
