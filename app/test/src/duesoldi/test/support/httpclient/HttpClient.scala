package duesoldi.test.support.httpclient

import cicerone._
import duesoldi.Env
import hammerspace.testing.StreamHelpers._
import ratatoskr.{Method, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object HttpClient
{
  def get(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("GET", path, env, headers)
  }

  def put(path: String, env: Env, body: String, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("PUT", path, env, headers, Some(body))
  }

  def delete(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("DELETE", path, env, headers)
  }

  def options(path: String, env: Env, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[Response] = {
    send("OPTIONS", path, env, headers)
  }

  def send(method: String, path: String, env: Env, headers: Seq[(String, String)], body: Option[String] = None)(implicit ec: ExecutionContext): Future[Response] = {
    new Client().send(ratatoskr.Request(
      method = Method(method),
      url = s"http://localhost:${env("PORT")}$path",
      body = body.map(_.asByteStream("UTF-8")).getOrElse(Stream.empty),
      headers = headers.map { case (name, value) => name -> Seq(value) } toMap
    )).map {
      case Right(response) => response
      case Left(cicerone.UnexpectedException(exception)) => throw exception
      case Left(failure) => throw new RuntimeException(s"HTTP request failed unexpectedly [$failure]")
    }
  }
}
