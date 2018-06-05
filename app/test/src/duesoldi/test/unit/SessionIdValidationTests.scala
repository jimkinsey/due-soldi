package duesoldi.test.unit

import duesoldi.app.sessions.SessionIdValidation.InvalidSessionId
import duesoldi.app.sessions.{SessionIdCreation, SessionIdValidation, UnvalidatedSessionId, ValidatedSessionId}
import hammerspace.testing.CustomMatchers._
import utest._

object SessionIdValidationTests
extends TestSuite
{
  val tests = this {
    "Session ID validation" - {
      "A session ID where the hashed details are not those used in the hash is invalid" - {
        val notJimSessionId = SessionIdCreation.createSessionId("s3cr3t")("charley")
        val result = SessionIdValidation.validateSessionId("s3cr3t")(UnvalidatedSessionId("jim", notJimSessionId.hash))
        assert(result isLeftOf InvalidSessionId)
      }
      "A session ID where the secret key is not correct is invalid" - {
        val otherSessionId = SessionIdCreation.createSessionId("s3cr3t")("jim")
        val result = SessionIdValidation.validateSessionId("0th3rs3cr3t")(UnvalidatedSessionId("jim", otherSessionId.hash))
        assert(result isLeftOf InvalidSessionId)
      }
      "A session ID where the hashed details are those used in the hash is valid" - {
        val charleySessionId = SessionIdCreation.createSessionId("s3cr3t")("charley")
        val result = SessionIdValidation.validateSessionId("s3cr3t")(UnvalidatedSessionId("charley", charleySessionId.hash))
        assert(result isRightOf ValidatedSessionId("charley", charleySessionId.hash))
      }
    }
  }
}

