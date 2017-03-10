package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import org.scalatest.AsyncWordSpec

class BlogEditingTests extends AsyncWordSpec with BlogStorage with Database with ServerSupport with ServerRequests with AdminSupport {
  import Setup.withSetup
  import org.scalatest.Matchers._

  "making a PUT request with a Markdown document" must {

    "create a blog entry at the specified ID where none already exists" in {
      withSetup(
        database,
        adminCredentials("admin", "password")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/new-entry", body = "# New entry!", headers = BasicAuthorization("admin", "password"))
            entryResponse  <- get("/blog/new-entry")
          } yield {
            createResponse.status shouldBe 201
            entryResponse.status  shouldBe 200
            entryResponse.body    should include("New entry!")
          }
        }
      }
    }

    "update the blog entry at the specified ID where it already exists" in {
      pending
      withSetup(
        database,
        adminCredentials("admin", "password"),
        blogEntries("entry" -> "# Inserted!")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/entry", body = "# Updated!", headers = BasicAuthorization("admin", "password"))
            entryResponse  <- get("/blog/entry")
          } yield {
            createResponse.status shouldBe 200
            entryResponse.status  shouldBe 200
            entryResponse.body    should include("Updated!")
          }
        }
      }
    }

    "return a bad request response when the id is invalid" in {
      withSetup(
        database,
        adminCredentials("admin", "password")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/';+DROP+TABLE+blog_entry;", body = "# Attack!", headers = BasicAuthorization("admin", "password"))
          } yield {
            createResponse.status shouldBe 400
          }
        }
      }
    }

    "return a bad request response when the document does not have a level 1 header" in {
      withSetup(
        database,
        adminCredentials("admin", "password")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/untitled", body = "_Intentionally left blank_", headers = BasicAuthorization("admin", "password"))
          } yield {
            createResponse.status shouldBe 400
          }
        }
      }
    }

    "not allow creation where no credentials are supplied" in {
      withSetup(
        database,
        adminCredentials("admin", "password")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/new-entry", body = "# New entry!")
          } yield {
            createResponse.status shouldBe 401
          }
        }
      }
    }

    "not allow creation where the wrong credentials are supplied" in {
      withSetup(
        database,
        adminCredentials("admin", "password")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- put("/admin/blog/new-entry", body = "# New entry!", headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            createResponse.status shouldBe 401
          }
        }
      }
    }

  }

  "making a DELETE request to a blog entry URL" must {

    "delete the blog entry at the specified ID where it already exists" in {
      withSetup(
        database,
        adminCredentials("admin", "password"),
        blogEntries("existing" -> "# Existing!")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- delete("/admin/blog/existing", headers = BasicAuthorization("admin", "password"))
            entryResponse  <- get("/blog/existing")
          } yield {
            createResponse.status shouldBe 204
            entryResponse.status  shouldBe 404
          }
        }
      }
    }

    "require admin priviliges" in {
      withSetup(
        database,
        adminCredentials("admin", "password"),
        blogEntries("any-entry" -> "# Title")
      ) {
        withServer { implicit server =>
          for {
            createResponse <- delete("/admin/blog/any-entry", headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            createResponse.status shouldBe 401
          }
        }
      }
    }

  }

}
