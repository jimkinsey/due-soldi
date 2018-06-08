package sommelier.implementation

import com.sun.net.httpserver.HttpExchange
import hammerspace.streams.InputStreams
import ratatoskr.{Method, Request}
import sommelier.Response
import sommelier.serving.HttpMessageContext

import scala.collection.JavaConverters._

class HttpExchangeMessageContext(exchange: HttpExchange)
extends HttpMessageContext
{
  // FIXME make this safer by preventing subsequent invocations of get and send from erroring
  def get: Request = {
    val url = exchange.getRequestURI.toString
    val method: Method = exchange.getRequestMethod match {
      case "HEAD" => Method.GET
      case other => Method(other)
    }
    val headers: Map[String, Seq[String]] =
      exchange
        .getRequestHeaders
        .entrySet().asScala
        .toSeq
        .map(entry => entry.getKey -> entry.getValue.asScala)
        .toMap
    val body: Stream[Byte] =
      exchange.getRequestMethod match {
        case "GET" | "HEAD" | "DELETE" => Stream.empty
        case _ => InputStreams.toByteStream(exchange.getRequestBody)
      }
    Request(
      method = method,
      url = url,
      headers = headers,
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
