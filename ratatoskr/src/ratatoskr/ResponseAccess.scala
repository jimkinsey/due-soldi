package ratatoskr

import hammerspace.collections.MapEnhancements._

object ResponseAccess
{
  implicit class ResponseAccessor(response: Response)
  {
    def header(name: String): Option[Seq[String]] = response.headers.lowKeys.get(name.toLowerCase)

    def cookie(name: String): Option[Cookie] = {
      for {
        setCookies <- header("Set-Cookie")
        namedCookie <- setCookies.find(_.startsWith(s"$name="))
        value = namedCookie.substring(s"$name=".length)
      } yield {
        Cookie(name, value)
      }
    }
  }
}
