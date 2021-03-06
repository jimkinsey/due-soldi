package duesoldi.app

package object sessions
{
  sealed trait SessionId
  {
    def user: String
    def hash: String
    final def formatted = s"user:$user,h:$hash"
  }
  case class UnvalidatedSessionId(user: String, hash: String) extends SessionId
  case class ValidatedSessionId(user: String, hash: String) extends SessionId
}
