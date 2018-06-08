package ratatoskr

import hammerspace.testing.StreamHelpers._
import hammerspace.uri.URI
import hammerspace.collections.MapEnhancements._

import scala.util.matching.Regex

case class Request(method: Method, url: String, body: Stream[Byte] = Stream.empty, headers: Headers = EmptyHeaders)

object RequestBuilding
{
  implicit def methodToBuilder(method: Method): RequestBuilder = RequestBuilder(Request(method, "/"))

  implicit class RequestBuilder(request: Request)
  {
    def apply(url: String): Request = {
      request.copy(url = url)
    }

    def apply(url: String, body: String): Request = {
      request.copy(url = url, body = body.asByteStream("UTF-8"))
    }

    def header(header: (String, String)): Request = {
      request.copy(headers = addHeader(header, request.headers))
    }

    def content(content: String): Request = {
      request.copy(body = content.asByteStream("UTF-8"))
    }
  }
}

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
