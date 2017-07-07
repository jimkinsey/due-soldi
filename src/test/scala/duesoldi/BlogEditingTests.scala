package duesoldi

import duesoldi.httpclient.BasicAuthorization
import duesoldi.storage.{BlogStorage, Database}
import duesoldi.testapp.{ServerRequests, ServerSupport}
import Setup.withSetup
import utest._

object BlogEditingTests 
  extends TestSuite 
  with BlogStorage 
  with Database 
  with ServerSupport 
  with ServerRequests 
  with AdminSupport 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "making a PUT request with a Markdown document" - {
      "create a blog entry at the specified ID where none already exists" - {
        withSetup(
          database,
          adminCredentials("admin", "password")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- put("/admin/blog/new-entry", body = "# New entry!", headers = BasicAuthorization("admin", "password"))
              entryResponse <- get("/blog/new-entry")
            } yield {
              assert(createResponse.status == 201)
              assert(entryResponse.status == 200)
              assert(entryResponse.body contains "New entry!")
            }
          }
        }
      }
      "return a bad request response when the id is invalid" - {
        withSetup(
          database,
          adminCredentials("admin", "password")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- put("/admin/blog/';+DROP+TABLE+blog_entry;", body = "# Attack!", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(createResponse.status == 400)
            }
          }
        }
      }
      "return a bad request response when the document does not have a level 1 header" - {
        withSetup(
          database,
          adminCredentials("admin", "password")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- put("/admin/blog/untitled", body = "_Intentionally left blank_", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(createResponse.status == 400)
            }
          }
        }
      }
      "not allow creation where no credentials are supplied" - {
        withSetup(
          database,
          adminCredentials("admin", "password")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- put("/admin/blog/new-entry", body = "# New entry!")
            } yield {
              assert(createResponse.status == 401)
            }
          }
        }
      }
      "not allow creation where the wrong credentials are supplied" - {
        withSetup(
          database,
          adminCredentials("admin", "password")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- put("/admin/blog/new-entry", body = "# New entry!", headers = BasicAuthorization("not-an-admin", "password"))
            } yield {
              assert(createResponse.status == 401)
            }
          }
        }
      }
    }
    "making a DELETE request to a blog entry URL" - {
      "delete the blog entry at the specified ID where it already exists" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          blogEntries("existing" -> "# Existing!")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- delete("/admin/blog/existing", headers = BasicAuthorization("admin", "password"))
              entryResponse <- get("/blog/existing")
            } yield {
              assert(createResponse.status == 204)
              assert(entryResponse.status == 404)
            }
          }
        }
      }
      "require admin priviliges" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          blogEntries("any-entry" -> "# Title")
        ) {
          withServer { implicit server =>
            for {
              createResponse <- delete("/admin/blog/any-entry", headers = BasicAuthorization("not-an-admin", "password"))
            } yield {
              assert(createResponse.status == 401)
            }
          }
        }
      }
    }
    "making a GET request to a blog entry URL" - {
      "return a 404 when the blog entry does not exist" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          blogEntries()) {
          withServer { implicit server =>
            for {
              getResponse <- get("/admin/blog/does-not-exist", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(getResponse.status == 404)
            }
          }
        }
      }
      "return the blog entry markdown doc when it does exist" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          blogEntries("does-exist" -> "# Title")) {
          withServer { implicit server =>
            for {
              getResponse <- get("/admin/blog/does-exist", headers = BasicAuthorization("admin", "password"))
            } yield {
              assert(getResponse.status == 200)
              assert(getResponse.body == "# Title")
            }
          }
        }
      }
      "require admin privileges" - {
        withSetup(
          database,
          adminCredentials("admin", "password"),
          blogEntries()) {
          withServer { implicit server =>
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
}
