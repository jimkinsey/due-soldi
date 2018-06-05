package duesoldi.app.sessions

import duesoldi.app.sessions.SessionIdParsing.ParseFailure.MalformedSessionId

object SessionIdParsing
{
  case class SessionId(user: String, hash: String)

  sealed trait ParseFailure
  object ParseFailure
  {
    case object MalformedSessionId extends ParseFailure
  }

  def parseSessionId(raw: String): Either[ParseFailure, SessionId] = {
    raw match {
      case WellFormedSessionId(user, hash) => Right(SessionId(user, hash))
      case _ => Left(MalformedSessionId)
    }
  }

  private val WellFormedSessionId = """^user:([\w]+);h:(.+)$""".r
}
