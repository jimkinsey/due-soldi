package duesoldi.app.sessions

object SessionIdValidation
{
  case object InvalidSessionId

  def validateSessionId(secret: String)(sessionId: UnvalidatedSessionId): Either[InvalidSessionId.type, ValidatedSessionId] = {
    val validSessionId = SessionIdCreation.createSessionId(secret)(sessionId.user)
    if (validSessionId.hash == sessionId.hash)
      Right(ValidatedSessionId(sessionId.user, sessionId.hash))
    else
      Left(InvalidSessionId)
  }
}
