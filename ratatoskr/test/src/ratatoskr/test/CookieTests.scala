package ratatoskr.test

import ratatoskr.Cookie
import utest._

object CookieTests
extends TestSuite
{
  val tests = this {
    "formatting to a request header" - {
      val cookie = Cookie("name", "value")
      assert(cookie.toRequestHeader == "Cookie" -> "name=value")
    }
    "Parsing a cookie string" - {
      "yields a cookie when the string is a simple name-value pair" - {
        val result = Cookie.parse("name=value")
        assert(result == Cookie("name", "value"))
      }
    }
  }
}
