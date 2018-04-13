package duesoldi.test.support.httpclient

import cicerone._
import duesoldi.Env

import hammerspace.testing.StreamHelpers._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object HttpClient
{
  case class Response(status: Int, headers: Map[String, Seq[String]], body: String)

  def get(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("GET", path, env, headers)
  }

  def put(path: String, env: Env, body: String, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("PUT", path, env, headers, Some(body))
  }

  def delete(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("DELETE", path, env, headers)
  }

  def send(method: String, path: String, env: Env, headers: Seq[(String, String)], body: Option[String] = None)(implicit ec: ExecutionContext): Future[Response] = {
    new Client().send(cicerone.Request(
      method = method,
      url = s"http://localhost:${env("PORT")}$path",
      body = body,
      headers = headers.map { case (name, value) => name -> Seq(value) } toMap
    )).map { result =>
      result.map { response =>
        Response(
          status = response.status,
          headers = response.headers,
          body = response.body.asString
        )
      } match {
        case Right(response) => response
        case Left(cicerone.UnexpectedException(exception)) => throw exception
        case Left(failure) => throw new RuntimeException(s"HTTP request failed unexpectedly [$failure]")
      }
    }
  }

}