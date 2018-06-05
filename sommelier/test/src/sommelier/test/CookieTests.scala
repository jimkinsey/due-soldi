package sommelier.test

import sommelier.messaging.Cookie
import utest._

object CookieTests
extends TestSuite
{
  val tests = this {
    "Parsing a cookie string" - {
      "yields a cookie when the string is a simple name-value pair" - {
        val result = Cookie.parse("name=value")
        assert(result == Cookie("name", "value"))
      }
    }
  }
}
