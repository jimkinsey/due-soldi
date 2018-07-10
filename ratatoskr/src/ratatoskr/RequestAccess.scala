package ratatoskr

import java.net.URLDecoder

import hammerspace.collections.MapEnhancements._
import hammerspace.testing.StreamHelpers._
import hammerspace.uri.URI

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
        .map(parseParams)
        .getOrElse(Map.empty)

    def formValues: Map[String, Seq[String]] = parseParams(request.body.asString)
  }

  private def parseParams(in: String): Map[String, Seq[String]] =
    in
      .split('&')
      .foldLeft[Map[String, Seq[String]]](Map.empty) {
        case (acc, Param(key, value)) if acc.contains(key) =>
          acc ++ Map(key -> (acc(key) :+ value))
        case (acc, Param(key, value)) =>
          acc ++ Map(key -> Seq(value))
        case (acc, _) =>
          acc
      }
      .mapValues(_.map(unescapeParamValue))

  private lazy val Param: Regex = """^(.+)=(.+)$""".r

  private def unescapeParamValue(value: String): String = {
    URLDecoder.decode(value.replace('+', ' '), "UTF-8")
  }
}
