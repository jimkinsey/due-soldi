package duesoldi.test.unit

import duesoldi.app.sessions.SessionIdCreation
import utest._

object SessionIdCreationTests
extends TestSuite
{
  val tests = this {
    "Session ID creation" - {
      "Creates a session ID with the provided details hashed with the secret" - {
        val expectedHash = md5("user:charley;secret:s3cr3t")
        val sessionId = SessionIdCreation.createSessionId("s3cr3t")("charley")
        assert(
          sessionId.user == "charley",
          sessionId.hash == expectedHash
        )
      }
    }
  }

  private def md5(string: String): String = {
    import java.security.MessageDigest
    val messageBytes = string.getBytes("UTF-8")
    val digestBytes = MessageDigest.getInstance("MD5").digest(messageBytes)
    new String(digestBytes, "UTF-8")
  }
}
