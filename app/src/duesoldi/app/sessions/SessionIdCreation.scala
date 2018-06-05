package duesoldi.app.sessions

import duesoldi.app.sessions.SessionIdParsing.SessionId

object SessionIdCreation
{
  def createSessionId(secret: String)(user: String): SessionId = {
    SessionId(user, md5(s"user:$user;secret:$secret"))
  }

  private def md5(string: String): String = {
    import java.security.MessageDigest
    val bytesOfMessage = string.getBytes("UTF-8")
    val md = MessageDigest.getInstance("MD5")
    val thedigest = md.digest(bytesOfMessage)
    new String(thedigest, "UTF-8")
  }
}
