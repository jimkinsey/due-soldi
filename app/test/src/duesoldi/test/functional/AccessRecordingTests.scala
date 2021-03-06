package duesoldi.test.functional

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.Env
import duesoldi.test.support.app.ServerRequests._
import duesoldi.test.support.app.TestApp
import duesoldi.test.support.app.TestApp.runningAppForThisTestOnly
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.setup.BlogStorage._
import duesoldi.test.support.setup.Database._
import duesoldi.test.support.setup.Setup.withSetup
import duesoldi.test.support.setup.SyncSetup
import hammerspace.testing.CustomMatchers._
import hammerspace.testing.StreamHelpers._
import ratatoskr.ResponseAccess._
import utest._

import scala.util.matching.Regex

object AccessRecordingTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "the access CSV endpoint" - {
      "returns a 403 for unauthorised access" - {
        withSetup(
          runningAppForThisTestOnly
        ) { implicit env =>
          for {
            response <- get("/admin/metrics/access.csv", headers = BasicAuthorization("admin", "wrong-password"))
          } yield {
            assert(response.status == 403)
          }
        }
      }
      "serves a CSV file with a header when there are no metrics recorded" - {
        withSetup(
          database,
          runningAppForThisTestOnly
        ) { implicit env =>
          for {
            response <- get("/admin/metrics/access.csv", headers = TestApp.adminAuth)
          } yield {
            assert(
              response.status == 200,
              response.body.asString.lines.toList.head == "Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code",
              response.body.asString.lines.toList.tail == Nil
            )
          }
        }
      }
      "serves a CSV file containing a row for each blog entry page request" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.csv", headers = TestApp.adminAuth)
          } yield {
            assert(
              response.body.asString.lines exists(_.contains("/blog/id"))
            )
          }
        }
      }
      "serves a CSV file containing a row for each blog index page request" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntry("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/", headers =
              "Referer" -> "http://altavista.is",
              "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
              "Cf-Connecting-Ip" -> "1.2.3.4",
              "Cf-Ipcountry" -> "IS"
            )
            response <- get("/admin/metrics/access.csv", headers = TestApp.adminAuth)
          } yield {
            assert(response.body.asString.lines exists(_.contains("/blog/")))
          }
        }
      }
      "includes data from the CDN headers in the records" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntry("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/", headers =
              "Cf-Connecting-Ip" -> "1.2.3.4",
              "Cf-Ipcountry" -> "IS"
            )
            response <- get("/admin/metrics/access.csv", headers = TestApp.adminAuth)
          } yield {
            assert(
              response.body.asString.lines exists(_.contains("1.2.3.4")),
              response.body.asString.lines exists(_.contains("IS"))
            )
          }
        }
      }
      "has no records for periods when access recording is disabled" - {
        withSetup(
          database,
          accessRecordingDisabled,
          runningAppForThisTestOnly,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/")
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.csv", headers = TestApp.adminAuth)
          } yield {
            assert(response.body.asString.lines.toList.tail.isEmpty)
          }
        }
      }
      "returns no records if the start timestamp parameter is in the future" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/")
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.csv?start=2099-10-12T00:00:00Z", headers = TestApp.adminAuth)
          } yield {
            assert(response.body.asString.lines.toList.tail.isEmpty)
          }
        }
      }
    }
    "The access JSON endpoint" - {
      "returns a JSON file containing access records" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.json", headers = TestApp.adminAuth)
          } yield {
            assert(
              response.headers.get("Content-type") exists(_ exists(_ contains "application/json")),
              response.body.asString contains """"path": "/blog/id","""
            )
          }
        }
      }
      "returns JSON with dates formatted for easy parsing in JavaScript" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("id" -> "# Content!")
        ) { implicit env =>
          for {
            _ <- get("/blog/id")
            response <- get("/admin/metrics/access.json", headers = TestApp.adminAuth)
            timestamp = jsonStringField("time").findFirstMatchIn(response.body.asString).get group 1
          } yield {
            assert(
              timestamp hasDateFormat "YYYY-MM-dd'T'HH:mm:ss"
            )
          }
        }
      }
      "takes the start date in a format convenient for a JavaScript client to send" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("distant-past" -> "# Content 1!", "not-so-distant-past" -> "# Content 2!")
        ) { implicit env =>
          for {
            _ <- get("/blog/distant-past")
            betweenAccesses = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'").format(ZonedDateTime.now(ZoneId.of("UTC")))
            _ <- get("/blog/not-so-distant-past")
            response <- get(s"/admin/metrics/access.json?start=$betweenAccesses", headers = TestApp.adminAuth)
            body = response.body.asString
          } yield {
            assert(
              !(body contains "/blog/distant-past"),
              body contains "/blog/not-so-distant-past"
            )
          }
        }
      }
      "allows filtering by path" - {
        withSetup(
          database,
          accessRecordingEnabled,
          runningAppForThisTestOnly,
          blogEntries("entry-one" -> "# Entry 1!", "entry-two" -> "# Entry 2!")
        ) { implicit env =>
          for {
            _ <- get("/blog/entry-one")
            _ <- get("/blog/entry-two")
            response <- get(s"/admin/metrics/access.json?path=/blog/entry-one", headers = TestApp.adminAuth)
            body = response.body.asString
          } yield {
            assert(
              !(body contains "/blog/entry-two"),
              body contains "/blog/entry-one"
            )
          }
        }
      }
      "supports session-based access" - {
        withSetup(
          database,
          runningAppForThisTestOnly
        ) { implicit env =>
          for {
            firstAccess <- get("/admin/metrics/access.json", headers = TestApp.adminAuth)
            sessionIdCookie = firstAccess.cookie("adminSessionId")
            _ = assert(sessionIdCookie isDefined)
            secondAccess <- get("/admin/metrics/access.json", headers = sessionIdCookie.get.toRequestHeader)
            thirdAccess <- get("/admin/metrics/access.json", headers = sessionIdCookie.get.toRequestHeader)
            fourthAccess <- get("/admin/metrics/access.json")
          } yield {
            assert(
              firstAccess.status == 200,
              secondAccess.status == 200,
              thirdAccess.status == 200,
              fourthAccess.status == 401
            )
          }
        }
      }
      "Allows CORS access for all origins" - {
        withSetup(
          database,
          runningAppForThisTestOnly
        ) { implicit env =>
          for {
            optionsResponse <- options("/admin/metrics/access.json", headers = "Foo" -> "Bar", "Access-Control-Request-Headers" -> "Foo")
            response <- get("/admin/metrics/access.json", headers = TestApp.adminAuth)
          } yield {
            assert(
              optionsResponse.header("Access-Control-Allow-Origin") contains Seq("*"),
              optionsResponse.header("Access-Control-Allow-Methods") contains Seq("GET"),
              optionsResponse.header("Access-Control-Allow-Headers") exists (_.nonEmpty),
              response.header("Access-Control-Allow-Origin") contains Seq("*")
            )
          }
        }
      }
    }
    "The access overview page" - {
      "is only available to admins" - {
        withSetup(runningAppForThisTestOnly) { implicit env =>
          for {
            accessPageWithAuth <- get("/admin/metrics/access/", headers = TestApp.adminAuth)
            sessionIdCookie = accessPageWithAuth.cookie("adminSessionId")
            _ = assert(sessionIdCookie isDefined)
            accessPageWithCookie <- get("/admin/metrics/access/", headers = sessionIdCookie.get.toRequestHeader)
            accessPageUnauthorised <- get("/admin/metrics/access/")
          } yield {
            assert(
              accessPageWithAuth.status == 200,
              accessPageWithCookie.status == 200,
              accessPageUnauthorised.status == 401,
              accessPageUnauthorised.header("WWW-Authenticate") nonEmpty
            )
          }
        }
      }
      "returns HTML" - {
        withSetup(runningAppForThisTestOnly) { implicit env =>
          for {
            response <- get("/admin/metrics/access/", headers = TestApp.adminAuth)
          } yield {
            assert(
              response.status == 200,
              response.header("Content-Type") contains Seq("text/html; charset=UTF-8"),
              response.body nonEmpty
            )
          }
        }
      }
    }
  }

  private lazy val accessRecordingEnabled = new SyncSetup {
    override def setup(env: Env) = Map("ACCESS_RECORDING_ENABLED" -> "true")
  }

  private lazy val accessRecordingDisabled = new SyncSetup {
    override def setup(env: Env) = Map("ACCESS_RECORDING_ENABLED" -> "false")
  }

  private def jsonStringField(name: String): Regex = s""".*"$name": "(.+?)".*""".r
}
