package duesoldi.test.unit

import duesoldi.app.sessions.{SessionIdCreation, Sessions}
import duesoldi.test.support.httpclient.BasicAuthorization
import ratatoskr.{Cookie, Method}
import sommelier.messaging.Request
import utest._

object SessionsTests
extends TestSuite
{
  val tests = this {
    "Getting the session cookie for a request" - {
      "returns the existing cookie when present and valid" - {
        val sessionId = SessionIdCreation.createSessionId("s3cr3t")("jim")
        val request = Request(Method.GET, "/", headers = Map("cookie" -> Seq(s"adminSessionId=${sessionId.formatted}")))
        val cookie = Sessions.getSessionCookie("s3cr3t")(request)

        assert(cookie contains Cookie("adminSessionId", sessionId.formatted))
      }
      "returns a new cookie when the cookie is absent and basic auth is present" - {
        val sessionId = SessionIdCreation.createSessionId("s3cr3t")("jim")
        val (name, value) = BasicAuthorization("jim", "p455w0rd")
        val request = Request(Method.GET, "/", headers = Map(name -> Seq(value)))
        val cookie = Sessions.getSessionCookie("s3cr3t")(request)

        assert(cookie contains Cookie("adminSessionId", sessionId.formatted))
      }
      "returns no cookie when the cookie is present but is invalid" - {
        val request = Request(Method.GET, "/", headers = Map("cookie" -> Seq(s"adminSessionId=NOT_A_VALID_SESSION_ID  ")))
        val cookie = Sessions.getSessionCookie("s3cr3t")(request)

        assert(cookie isEmpty)
      }
    }
  }
}
