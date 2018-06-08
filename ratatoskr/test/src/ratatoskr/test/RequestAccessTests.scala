package ratatoskr.test

import ratatoskr.{Cookie, Method, Request}
import ratatoskr.RequestAccess._
import utest._

object RequestAccessTests
extends TestSuite
{
  val tests = this {
    "Accessing cookies" - {
      "returns None when there is no matching cookie header for the provided name" - {
        val request = Request(Method.GET, "/", headers = ratatoskr.EmptyHeaders)
        val cookie = request.cookie("sessionId")
        assert(cookie isEmpty)
      }
      "returns a cookie when there is a matching cookie header for the provided name" - {
        val request = Request(Method.GET, "/", headers = Map("Cookie" -> Seq("sessionId=foo")))
        val cookie = request.cookie("sessionId")
        assert(cookie contains Cookie("sessionId", "foo"))
      }
    }
    "Getting the path" - {
      "returns the path from the request URI" - {
        val request = Request(Method.GET, "http://host:8080/path/to/some.where")
        val path = request.path
        assert(path == "/path/to/some.where")
      }
    }
    "Getting a header" - {
      "returns none when the header is not present" - {
        val request = Request(Method.GET, "/", headers = ratatoskr.EmptyHeaders)
        val header = request.header("foo")
        assert(header isEmpty)
      }
      "returns the header value when present" - {
        val request = Request(Method.GET, "/", headers = Map("foo" -> Seq("42")))
        val header = request.header("foo")
        assert(header contains Seq("42"))
      }
      "treats header names as case-insensitive" - {
        val request = Request(Method.GET, "/", headers = Map("foo" -> Seq("42")))
        val header = request.header("fOo")
        assert(header contains Seq("42"))
      }
    }
  }
}
