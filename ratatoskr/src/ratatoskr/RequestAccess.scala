package ratatoskr

import hammerspace.uri.URI
import hammerspace.collections.MapEnhancements._

import scala.util.matching.Regex

object RequestAccess
{
  implicit class RequestAccessor(request: Request)
  {
    def header(name: String): Option[Seq[String]] = request.headers.lowKeys.get(name.toLowerCase)

    def cookie(name: String): Option[Cookie] = {
      for {
        setCookies <- request.headers.find(_._1.toLowerCase == "cookie").map(_._2)
        namedCookie <- setCookies.find(_.startsWith(s"$name="))
        value = namedCookie.substring(s"$name=".length)
      } yield {
        Cookie(name, value)
      }
    }

    def path: String = URI.parse(request.url).path

    def queryParams: Map[String, Seq[String]] =
      URI.parse(request.url).queryString
        .map(_
          .split('&')
          .foldLeft[Map[String, Seq[String]]](Map.empty) {
            case (acc, QueryParam(key, value)) if acc.contains(key) =>
              acc ++ Map(key -> (acc(key) :+ value))
            case (acc, QueryParam(key, value)) =>
              acc ++ Map(key -> Seq(value))
            case (acc, _) =>
              acc
          }
        )
        .getOrElse(Map.empty)

    private lazy val QueryParam: Regex = """^(.+)=(.+)$""".r
  }

}
