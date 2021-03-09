package ratatoskr

import hammerspace.collections.MapEnhancements._
import hammerspace.collections.StreamEnhancements.EnhancedStream
import hammerspace.testing.StreamHelpers._
import hammerspace.uri.URI

import java.net.URLDecoder
import scala.util.matching.Regex

case class MultipartFormValue(
  name: String,
  data: Stream[Byte],
  contentType: Option[String] = None
)

object RequestAccess
{
  implicit class RequestAccessor(request: Request) {
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

    def multipartFormValues: Stream[MultipartFormValue] = {
      request.header("Content-Type")
        .flatMap(_.headOption)
        .collect { case Boundary(b) => s"\n--$b".getBytes }
        .fold[Stream[MultipartFormValue]](Stream.empty) { boundary =>
          request.body
            .separatedBy(boundary)
            .foldLeft[Stream[MultipartFormValue]](Stream.empty) { case (acc, chunk) =>
              val firstLine = new String(chunk.dropWhile(_ == '\n').takeWhile(_ != '\n').toArray)

              firstLine match {
                case ContentDisposition(name) =>
                  val remainder = chunk.drop(firstLine.length + 1)
                  val secondLine = new String(remainder.dropWhile(_ == '\n').takeWhile(_ != '\n').toArray)

                  val (contentType, data) = secondLine match {
                    case ContentType(typ) =>
                      (Some(typ), remainder.removePrefix(secondLine.getBytes()).dropWhile(_.toChar.isWhitespace))
                    case _ =>
                      (None, remainder.dropWhile(_.toChar.isWhitespace))
                  }

                  acc.append(Stream(MultipartFormValue(name, data, contentType)))
                case _ =>
                  acc
              }
            }
        }
    }

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

  private lazy val Boundary = """.*boundary=(.+)$""".r
  private lazy val ContentDisposition = """^Content-Disposition:\s*form-data;\s*name="(.+)"\s*$""".r
  private lazy val ContentType = """^Content-Type:\s*(.+)\s*""".r

}
