package cicerone

import java.net._

import hammerspace.streams.InputStreams
import ratatoskr.{Request, Response}

import scala.concurrent.duration.Duration
import scala.util.Try
import scala.collection.JavaConverters._

private[cicerone] object HttpConnection
{
  case class Configuration(connectTimeout: Duration = Duration(5, "seconds"), followRedirects: Boolean = false)

  def open(url: String): Either[Failure, HttpURLConnection] = {
    Try(new URL(url).openConnection().asInstanceOf[HttpURLConnection])
      .toEither
      .left.map {
        case _: MalformedURLException => MalformedURL
        case exception: Exception => UnexpectedException(exception)
      }
  }

  def configure(connection: HttpURLConnection, configuration: Configuration): Unit = {
    connection.setConnectTimeout(configuration.connectTimeout.toMillis.toInt)
    connection.setInstanceFollowRedirects(configuration.followRedirects)
  }

  def applyRequest(connection: HttpURLConnection, request: Request): Option[Failure] = {
    Try {
      connection.setRequestMethod(request.method.toString)
      request.headers.foreach { case (key, values) =>
        connection.setRequestProperty(key, values.mkString(","))
      }
      if (request.body.nonEmpty) {
        connection.setDoOutput(true)
        connection.getOutputStream.write(request.body.toArray)
        connection.getOutputStream.close()
      }
    }
      .toEither
      .left.map {
        case exception: Exception => UnexpectedException(exception)
      }
      .swap
      .toOption
  }

  def retrieveResponse(connection: HttpURLConnection): Either[Failure, Response] = {
    Try {
      val headers: Headers = connection.getHeaderFields.asScala.filter(_._1 != null).foldLeft[Headers](Map.empty) {
        case (acc, (key, values)) => acc ++ Map(key -> values.asScala)
      }
      val bodyStream = Option(connection.getResponseCode match {
        case code if code >= 400 => connection.getErrorStream
        case _ => connection.getInputStream
      })
      Response(
        status = connection.getResponseCode,
        headers = headers,
        body = bodyStream.map(stream => InputStreams.toByteStream(stream, onClose = {
          connection.disconnect()
        })).getOrElse(Stream.empty)
      )
    }
      .toEither
      .left.map {
        case exception: Exception => UnexpectedException(exception)
      }
  }

  def connect(connection: HttpURLConnection): Option[Failure] = {
    Try(connection.connect())
      .toEither
      .left.map {
        case _: SocketTimeoutException => ConnectionFailure
        case _: ConnectException => ConnectionFailure
        case exception: Exception => UnexpectedException(exception)
      }
      .swap
      .toOption
  }
}
