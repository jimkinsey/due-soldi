package duesoldi.app.sessions

import hammerspace.security.Hashing

object SessionIdValidation
{
  case object InvalidSessionId

  def validateSessionId(secret: String)(sessionId: UnvalidatedSessionId): Either[InvalidSessionId.type, ValidatedSessionId] = {
    if (Hashing.md5(s"user:${sessionId.user};secret:$secret") == sessionId.hash)
      Right(ValidatedSessionId(sessionId.user, sessionId.hash))
    else
      Left(InvalidSessionId)
  }
}
