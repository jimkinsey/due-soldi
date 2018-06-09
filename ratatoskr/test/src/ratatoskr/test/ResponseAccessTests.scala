package ratatoskr.test

import ratatoskr.{Cookie, EmptyHeaders, Response}
import ratatoskr.ResponseAccess._
import utest._

object ResponseAccessTests
extends TestSuite
{
  val tests = this {
    "cookie" - {
      "returns None when the cookie list is empty" - {
        val response = Response(200, headers = EmptyHeaders)
        val cookie = response.cookie("sessionId")
        assert(cookie isEmpty)
      }
      "returns the named cookie when present in the Set-Cookie headers" - {
        val response = Response(200, headers = Map("Set-Cookie" -> Seq("sessionId=12345")))
        val cookie = response.cookie("sessionId")
        assert(cookie contains Cookie("sessionId", "12345"))
      }
      "the Set-Cookie header name is treated as case-insensitive" - {
        val response = Response(200, headers = Map("sEt-COOkiE" -> Seq("sessionId=12345")))
        val cookie = response.cookie("sessionId")
        assert(cookie isDefined)
      }
    }
    "header" - {
      "returns None when the named header is not present" - {
        val response = Response(200, headers = EmptyHeaders)
        val header = response.header("Content-Type")
        assert(header isEmpty)
      }
      "returns the header when present" - {
        val response = Response(200, headers = Map("Content-Type" -> Seq("text/plain")))
        val header = response.header("Content-Type")
        assert(header contains Seq("text/plain"))
      }
      "treats header names as case-insensitive" - {
        val response = Response(200, headers = Map("Content-Type" -> Seq("text/plain")))
        val header = response.header("ContENt-TYpE")
        assert(header contains Seq("text/plain"))
      }
    }
  }
}


