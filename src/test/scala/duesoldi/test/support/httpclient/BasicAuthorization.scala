package duesoldi.test.support.httpclient

import akka.parboiled2.util.Base64

object BasicAuthorization {
  def apply(username: String, password: String): (String, String) = "Authorization" -> s"Basic ${Base64.rfc2045().encodeToString(s"$username:$password".getBytes(), false)}"
}
