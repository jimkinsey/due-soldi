package duesoldi

import duesoldi.Setup.withSetup
import duesoldi.storage.{BlogStorage, Database, Images}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers._

class BlogAssetServingTests extends AsyncWordSpec with BlogStorage with Database with Images with ServerSupport with ServerRequests {

  "Getting a blog entry asset" must {

    "return a 200 response and the asset content when the entry and asset exist" in {
      withSetup(
        database,
        images("/blog/has-image/images/a-cat.gif" -> 200),
        blogEntries("has-image" -> "# A cat!")) {
        withServer { implicit server =>
          for {
            imageResponse <- get("/blog/has-image/images/a-cat.gif")
          } yield {
            imageResponse.status shouldBe 200
          }
        }
      }
    }

    "return a 404 response when the asset does not exist" in {
      withSetup(
        database,
        images("/blog/no-image/images/no-such-image.gif" -> 404),
        blogEntries("no-image" -> "# No image")) {
        withServer { implicit server =>
          for {
            imageResponse <- get("/blog/no-image/images/no-such-image.gif")
          } yield {
            imageResponse.status shouldBe 404
          }
        }
      }
    }

    "return a 404 response when the entry does not exist" in {
      withSetup(database) {
        withServer { implicit server =>
          for {
            imageResponse <- get("/blog/no-such-entry/images/image.gif")
          } yield {
            imageResponse.status shouldBe 404
          }
        }
      }
    }

    "return a 400 response for an invalid entry ID" in {
      withSetup(database) {
        withServer { implicit server =>
          for {
            imageResponse <- get("/blog/*Asd:*(/images/image.gif")
          } yield {
            imageResponse.status shouldBe 400
          }
        }
      }
    }

    "return a 502 (bad gateway) response for an image when the image server does so" in {
      withSetup(
        database,
        images("/blog/has-image/images/image.gif" -> 500),
        blogEntries("has-image" -> "# title")) {
        withServer { implicit server =>
          for {
            imageResponse <- get("/blog/has-image/images/image.gif")
          } yield {
            imageResponse.status shouldBe 502
          }
        }
      }
    }

  }

}
