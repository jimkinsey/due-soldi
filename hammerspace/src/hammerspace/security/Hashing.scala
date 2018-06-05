package hammerspace.security

import java.security.MessageDigest

object Hashing
{
  def md5(string: String): String = {
    val messageBytes = string.getBytes("UTF-8")
    val digestBytes = MessageDigest.getInstance("MD5").digest(messageBytes)
    new String(digestBytes, "UTF-8")
  }
}
