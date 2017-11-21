package duesoldi.test.functional

import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp
import duesoldi.test.support.app.TestApp.runningApp
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.setup.BlogStorage._
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.Setup.withSetup
import utest._

object BlogEditingTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "making a PUT request with a YAML document" - {
      "create a blog entry at the specified ID where none already exists" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/new-entry",
              body =
                s"""id: new-entry
                   |description:
                   |content: |
                   |    # New entry!
                   |""".stripMargin,
              headers = TestApp.adminAuth, "Content-Type" -> "text/x-yaml; charset=utf-8")
            entryResponse <- get("/blog/new-entry")
          } yield {
            assert(
              createResponse.status == 201,
              entryResponse.status == 200,
              entryResponse.body contains "New entry!"
            )
          }
        }
      }
      "return a bad request response when the id is invalid" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/';+DROP+TABLE+blog_entry;", body = "# Attack!", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "return a bad request response when the document does not have a level 1 header" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/untitled", body = "_Intentionally left blank_", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "not allow creation where no credentials are supplied" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/new-entry", body = "# New entry!")
          } yield {
            assert(createResponse.status == 401)
          }
        }
      }
      "not allow creation where the wrong credentials are supplied" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/new-entry",
              body = "# Doesn't matter!",
              headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            assert(createResponse.status == 401)
          }
        }
      }
      "not allow creation where the blog entry already exists" - {
        withSetup(
          database,
          runningApp,
          blogEntries(entry.withId("already-there"))
        ) { implicit env =>
          for {
            createResponse <- put("/admin/blog/already-there",
              body = entry.withId("already-there").toYaml,
              headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 409)
          }
        }
      }
    }
    "making a DELETE request to a blog entry URL" - {
      "delete the blog entry at the specified ID where it already exists" - {
        withSetup(
          database,
          runningApp,
          blogEntries("existing" -> "# Existing!")
        ) { implicit env =>
          for {
            createResponse <- delete("/admin/blog/existing", headers = TestApp.adminAuth)
            entryResponse <- get("/blog/existing")
          } yield {
            assert(createResponse.status == 204)
            assert(entryResponse.status == 404)
          }
        }
      }
      "returns a 400 when the id is invalid" - {
        withSetup(
          database,
          runningApp
        ) { implicit env =>
          for {
            createResponse <- delete("/admin/blog/';+DROP+TABLE+blog_entry;", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "require admin priviliges" - {
        withSetup(
          database,
          runningApp,
          blogEntries("any-entry" -> "# Title")
        ) { implicit env =>
          for {
            createResponse <- delete("/admin/blog/any-entry", headers = BasicAuthorization("not-an-admin", "password"))
          } yield {
            assert(createResponse.status == 401)
          }
        }
      }
    }
    "making a GET request to a blog entry URL" - {
      "returns a 404 when the blog entry does not exist" - {
        withSetup(
          database,
          runningApp,
          blogEntries()
        ) { implicit env =>
          for {
            getResponse <- get("/admin/blog/does-not-exist", headers = TestApp.adminAuth)
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
            createResponse <- get("/admin/blog/';+DROP+TABLE+blog_entry;", headers = TestApp.adminAuth)
          } yield {
            assert(createResponse.status == 400)
          }
        }
      }
      "returns the blog entry YAML doc when it does exist" - {
        withSetup(
          database,
          runningApp,
          blogEntries("does-exist" -> "# Title")
        ) { implicit env =>
          for {
            getResponse <- get("/admin/blog/does-exist", headers = TestApp.adminAuth)
          } yield {
            assert(
              getResponse.status == 200,
              getResponse.body contains "id: does-exist",
              getResponse.body contains "last-modified: ",
              getResponse.body contains "description: ",
              getResponse.body contains "content: |"
            )
          }
        }
      }
      "returns a blog entry YAML doc which is compatible with the PUT endpoint" - {
        withSetup(
          database,
          runningApp,
          blogEntries("id" -> "# Title")
        ) { implicit env =>
          for {
            getResponse <- get("/admin/blog/id", headers = TestApp.adminAuth)
            _ <- delete("/admin/blog/id", headers = TestApp.adminAuth)
            putResponse <- put("/admin/blog/id", getResponse.body, headers = TestApp.adminAuth)
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
          blogEntries()
        ) { implicit env =>
          for {
            getResponse <- get("/admin/blog/does-not-exist", headers = BasicAuthorization("not-admin", "password"))
          } yield {
            assert(getResponse.status == 401)
          }
        }
      }
    }
  }
}
