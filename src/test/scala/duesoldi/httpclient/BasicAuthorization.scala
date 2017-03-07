package duesoldi.httpclient

import akka.parboiled2.util.Base64

/**
  * Created by jimkinsey on 07/03/17.
  */
object BasicAuthorization {
  def apply(username: String, password: String): (String, String) = "Authorization" -> s"Basic ${Base64.rfc2045().encodeToString(s"$username:$password".getBytes(), false)}"
}
