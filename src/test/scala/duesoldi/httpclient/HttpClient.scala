package duesoldi.httpclient

import dispatch.{Http, url}
import duesoldi.Server

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object HttpClient {

  case class Response(status: Int, headers: Map[String, Seq[String]], body: String)

  def get(path: String, server: Server)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${server.port}$path")).map { res =>
      Response(
        status = res.getStatusCode,
        headers = convertHeaders(res.getHeaders),
        body = res.getResponseBody
      )
    }
  }

  private def convertHeaders(headers: java.util.Map[String, java.util.List[String]]): Map[String, Seq[String]] = {
    headers.asScala map { case (key, value) => key -> Seq(value:_*) } toMap
  }

}
