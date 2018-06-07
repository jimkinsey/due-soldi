package duesoldi.test.unit

import duesoldi.app.sessions.SessionIdParsing.ParseFailure.MalformedSessionId
import duesoldi.app.sessions.{SessionIdParsing, UnvalidatedSessionId}
import utest._
import hammerspace.testing.CustomMatchers._

object SessionIdParsingTests
extends TestSuite
{
  val tests = this {
    "Session ID parsing" - {
      "A malformed session ID cannot be parsed" - {
        val result = SessionIdParsing.parseSessionId("")
        assert(result isLeftOf MalformedSessionId)
      }
      "A well-formed session ID can be parsed" - {
        val result = SessionIdParsing.parseSessionId("user:jim,h:12345")
        assert(result isRightOf UnvalidatedSessionId("jim", "12345"))
      }
    }
  }
}
