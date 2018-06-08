package duesoldi.app.sessions

import java.util.Base64

import duesoldi.app.sessions.SessionIdParsing.parseSessionId
import duesoldi.app.sessions.SessionIdValidation.validateSessionId
import duesoldi.config.Config
import ratatoskr.{Cookie, Request}
import ratatoskr.RequestAccess._
import sommelier.messaging.Response
import sommelier.routing.{AuthorizationMatcher, Rejection}

object Sessions
{
  type GetSessionCookie = Request => Option[Cookie]

  def getSessionCookie(secret: String): GetSessionCookie = (request) => {
    sessionId(secret)(request).toOption.orElse {
      request.headers.get("Authorization").map { authHeader: Seq[String] =>
        val encodedAuth = authHeader.head.split(" ")(1)
        val decodedAuth = new String(Base64.getDecoder.decode(encodedAuth), "UTF-8")
        val user = decodedAuth.split(":")(0)
        SessionIdCreation.createSessionId(secret)(user)
      }
    } map { sessionId => Cookie("adminSessionId", sessionId.formatted)}
  }

  def validSession(implicit config: Config): AuthorizationMatcher = (request) => {
    sessionId(config.secretKey)(request).swap.toOption.map(message => Rejection(Response(403).body(message)))
  }

  def sessionId(secret: String)(request: Request): Either[String, ValidatedSessionId] = {
    for {
      cookie <- request.cookie("adminSessionId") toRight { "No session ID" }
      parsedSessionId <- parseSessionId(cookie.value).left map (_ => s"Malformed session ID")
      validatedSessionId <- validateSessionId(secret)(parsedSessionId).left map (_ => s"Invalid session ID")
    } yield {
      validatedSessionId
    }
  }
}


