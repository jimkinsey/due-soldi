package duesoldi.test.unit

import duesoldi.app.sessions.SessionIdCreation
import hammerspace.security.Hashing
import utest._

object SessionIdCreationTests
extends TestSuite
{
  val tests = this {
    "Session ID creation" - {
      "Creates a session ID with the provided details hashed with the secret" - {
        val expectedHash = Hashing.md5("user:charley;secret:s3cr3t")
        val sessionId = SessionIdCreation.createSessionId("s3cr3t")("charley")
        assert(
          sessionId.user == "charley",
          sessionId.hash == expectedHash
        )
      }
    }
  }
}
