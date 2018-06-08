package ratatoskr.test

import ratatoskr.{Cookie, Response}
import ratatoskr.ResponseAccess._
import utest._

object ResponseAccessTests
extends TestSuite
{
  val tests = this {
    "cookie returns None when the cookie list is empty" - {
      val response = Response(200, headers = ratatoskr.EmptyHeaders)
      val cookie = response.cookie("sessionId")
      assert(cookie isEmpty)
    }
    "cookie returns the named cookie when present in the Set-Cookie headers" - {
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
}


