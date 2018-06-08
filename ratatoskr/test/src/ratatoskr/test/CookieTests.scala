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
  }
}
