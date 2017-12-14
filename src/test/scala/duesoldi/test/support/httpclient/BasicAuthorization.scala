package duesoldi.test.support.httpclient

import java.util.Base64

object BasicAuthorization {
  def apply(username: String, password: String): (String, String) =
    "Authorization" -> s"Basic ${Base64.getEncoder.encodeToString(s"$username:$password".getBytes())}"
}
