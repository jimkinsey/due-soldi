package sommelier.implementation

import java.util.Scanner

import com.sun.net.httpserver.HttpExchange
import sommelier.serving.HttpMessageContext
import sommelier.{Method, Request, Response}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class HttpExchangeMessageContext(exchange: HttpExchange)
extends HttpMessageContext
{
  def get: Request = {
    lazy val method: Method = exchange.getRequestMethod match {
      case "HEAD" => Method.GET
      case other => Method(other)
    }
    lazy val queryParams: Map[String, Seq[String]] =
      Option(exchange.getRequestURI.getQuery)
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
    lazy val QueryParam: Regex = """^(.+)=(.+)$""".r
    lazy val headers: Map[String, Seq[String]] =
      exchange
        .getRequestHeaders
        .entrySet().asScala
        .toSeq
        .map(entry => entry.getKey -> entry.getValue.asScala)
        .toMap
    lazy val body: Option[String] =
      exchange.getRequestMethod match {
        case "GET" | "HEAD" | "DELETE" => None
        case _ =>
          val requestBody = exchange.getRequestBody
          val s = new Scanner(requestBody).useDelimiter("\\A")
          val result = if (s.hasNext) Some(s.next) else None
          requestBody.close()
          result
      }
    Request(
      method = method,
      path = exchange.getRequestURI.getPath,
      queryParams = queryParams,
      headers = headers,
      accept = Option(exchange.getRequestHeaders.getFirst("Accept")),
      body = body
    )
  }

  def send(response: Response): Unit = {
    response.headers.foreach { case (key, value) if value != null =>
      exchange.getResponseHeaders.add(key, value)
    }
    if (exchange.getRequestMethod != "HEAD") {
      exchange.sendResponseHeaders(response.status, response.body.map(_.length.toLong).getOrElse(0L))
      val os = exchange.getResponseBody
      response.body.foreach(os.write)
      os.close()
    }
    else {
      exchange.sendResponseHeaders(response.status, -1)
    }
  }
}
