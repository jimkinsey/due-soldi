package ratatoskr

import java.net.URLDecoder
import hammerspace.collections.MapEnhancements._
import hammerspace.testing.StreamHelpers._
import hammerspace.uri.URI

import java.util.Scanner
import scala.util.matching.Regex

case class MultipartFormValue(data: Stream[Byte])

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

    val Boundary = """.*boundary=(.+)$""".r
    val ContentDisposition = """^Content-Disposition:\s*form-data;\s*name="(.+)"\s*$""".r

    def multipartFormValues: Either[String,Map[String, Seq[MultipartFormValue]]] = {
      request.header("Content-Type")
        .flatMap(_.headOption)
        .collect { case Boundary(b) => s"\n--$b".getBytes }
        .toRight { s"No boundary specified in multi-part content-type header [${request.header("Content-Type").flatMap(_.headOption).getOrElse("n/a")}]" }
        .flatMap { boundary =>

          def loop(
            acc: Map[String, Seq[MultipartFormValue]],
            content: Stream[Byte]
          ): Either[String, Map[String, Seq[MultipartFormValue]]]= {
            if (content.length <= boundary.length + 1) {
              Right(acc)
            } else {

              if (content.indexOfSlice(boundary) == 0) {
                val remainder = content.drop(boundary.length + 1)

                val end = remainder.indexOfSlice(boundary)

                if (end > 0) {
                  val firstLine = new String(remainder.takeWhile(_ != '\n').toArray)

                  firstLine match {
                    case ContentDisposition(name) =>
                      val value = remainder.slice(firstLine.length, end).dropWhile(_ == '\n')
                      if (value.nonEmpty) {
                        acc.get(name) match {
                          case Some(seq) =>
                            loop(acc.updated(name, seq :+ MultipartFormValue(data = value)), remainder.drop(end))
                          case None =>
                            loop(acc + (name -> Seq(MultipartFormValue(data = value))), remainder.drop(end))
                        }
                      } else {
                        Left(s"No value data in multipart/form-data for [$name]")
                      }
                    case _ =>
                      Left(s"Invalid Content-Disposition line [$firstLine]")
                  }
                } else {
                  Left(s"Terminating boundary marker [${new String(boundary).trim().drop(2)}] not found")
                }
              } else {
                Left(s"Multi-part request body does not begin with boundary [${new String(boundary).trim().drop(2)}]")
              }
            }
          }

          loop(Map.empty, request.body)
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
}
