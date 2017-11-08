package duesoldi.test.support.httpclient

import dispatch.{Http, url}
import duesoldi.Env
import io.netty.handler.codec.http.HttpHeaders

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object HttpClient
{
  case class Response(status: Int, headers: Map[String, Seq[String]], body: String)

  def get(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${env("PORT")}$path").setHeaders(headers.map { case (name, value) => name -> Seq(value) } toMap)).map { res =>
      Response(
        status = res.getStatusCode,
        headers = convertHeaders(res.getHeaders),
        body = res.getResponseBody
      )
    }
  }

  def put(path: String, env: Env, body: String, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${env("PORT")}$path").setMethod("PUT").setBody(body).setHeaders(headers.map { case (name, value) => name -> Seq(value) } toMap)).map { res =>
      Response(
        status = res.getStatusCode,
        headers = convertHeaders(res.getHeaders),
        body = res.getResponseBody
      )
    }
  }

  def delete(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${env("PORT")}$path").setMethod("DELETE").setHeaders(headers.map { case (name, value) => name -> Seq(value) } toMap)).map { res =>
      Response(
        status = res.getStatusCode,
        headers = convertHeaders(res.getHeaders),
        body = res.getResponseBody
      )
    }
  }

  private def convertHeaders(headers: HttpHeaders): Map[String, Seq[String]] = {
    headers.entries().asScala.foldLeft(Map[String,Seq[String]]()) {
      case (acc, entry) if acc.contains(entry.getKey) => acc.updated(entry.getKey, acc(entry.getKey) :+ entry.getValue)
      case (acc, entry) => acc + (entry.getKey -> Seq(entry.getValue))
    }
  }
}
