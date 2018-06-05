package duesoldi.app.sessions

import duesoldi.app.sessions.SessionIdParsing.ParseFailure.MalformedSessionId

object SessionIdParsing
{
  sealed trait ParseFailure
  object ParseFailure
  {
    case object MalformedSessionId extends ParseFailure
  }

  def parseSessionId(raw: String): Either[ParseFailure, UnvalidatedSessionId] = {
    raw match {
      case WellFormedSessionId(user, hash) => Right(UnvalidatedSessionId(user, hash))
      case _ => Left(MalformedSessionId)
    }
  }

  private val WellFormedSessionId = """^user:([\w]+);h:(.+)$""".r
}
