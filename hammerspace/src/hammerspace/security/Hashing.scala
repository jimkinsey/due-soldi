package hammerspace.security

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hashing
{
  def hmac(secret: String)(string: String): String = {
    val sha256_HMAC = Mac.getInstance("HmacSHA256")
    val secret_key = new SecretKeySpec(secret.getBytes, "HmacSHA256")
    sha256_HMAC.init(secret_key)
    Base64.getEncoder.encodeToString(sha256_HMAC.doFinal(string.getBytes))
  }
}
