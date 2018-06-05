package duesoldi.app.sessions

import hammerspace.security.Hashing

object SessionIdCreation
{
  def createSessionId(secret: String)(user: String): ValidatedSessionId = {
    ValidatedSessionId(user, Hashing.md5(s"user:$user;secret:$secret"))
  }
}
