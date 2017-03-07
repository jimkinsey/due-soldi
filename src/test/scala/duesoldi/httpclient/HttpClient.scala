package duesoldi.httpclient

import dispatch.{Http, url}
import duesoldi.Server

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object HttpClient {

  case class Response(status: Int, headers: Map[String, Seq[String]], body: String)

  def get(path: String, server: Server, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${server.port}$path").setHeaders(headers.map { case (name, value) => name -> Seq(value) } toMap)).map { res =>
      Response(
        status = res.getStatusCode,
        headers = convertHeaders(res.getHeaders),
        body = res.getResponseBody
      )
    }
  }

  def put(path: String, server: Server, body: String, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${server.port}$path").setMethod("PUT").setBody(body).setHeaders(headers.map { case (name, value) => name -> Seq(value) } toMap)).map { res =>
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
