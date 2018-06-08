package ratatoskr

case class Response(status: Int, headers: Headers = EmptyHeaders, body: Stream[Byte] = Stream.empty)

object ResponseAccess
{
  implicit class ResponseAccessor(response: Response)
  {
    def cookie(name: String): Option[Cookie] = {
      for {
        setCookies <- response.headers.find(_._1.toLowerCase == "set-cookie").map(_._2)
        namedCookie <- setCookies.find(_.startsWith(s"$name="))
        value = namedCookie.substring(s"$name=".length)
      } yield {
        Cookie(name, value)
      }
    }
  }
}