package cicerone

import java.net.{HttpURLConnection, SocketTimeoutException, URL}

import duesoldi.streams.InputStreams

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Client(connectTimeout: Duration = Duration(5, "seconds"))
{
  def GET(url: String)(implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = Future {
    val conn: HttpURLConnection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    conn.setConnectTimeout(connectTimeout.toMillis.toInt)
    conn.setRequestMethod("GET")
    Try(conn.connect())
      .toEither
      .left.map {
        case _: SocketTimeoutException => ConnectionFailure
      }
      .map { _ =>
        val headers: Headers = conn.getHeaderFields.asScala.filter(_._1 != null).foldLeft[Headers](Map.empty) {
          case (acc, (key, values)) => acc ++ Map(key -> values.asScala)
        }
        val response = Response(
          status = conn.getResponseCode,
          headers = headers,
          body = InputStreams.toByteStream(conn.getInputStream, onClose = conn.disconnect())
        )
        response
      }
  }
}
