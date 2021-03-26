package duesoldi.test.support.app

import duesoldi.Env
import duesoldi.test.support.httpclient.HttpClient
import hammerspace.uri.URI
import ratatoskr.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

object ServerRequests
{
  def send(vanilla: Request)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    val environmentalised = vanilla.copy(url = URI.parse(vanilla.url).copy(scheme = "http", authority = s"localhost:${env("PORT")}").format, headers = addOverrides(vanilla.headers))
    cicerone.http(environmentalised).map {
      case Right(response) => response
      case Left(cicerone.UnexpectedException(exception)) => throw exception
      case Left(failure) => throw new RuntimeException(s"HTTP request failed unexpectedly [$failure]")
    }
  }

  def getNoFollow(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.get(path, env, withOverrides(headers))
  }

  def get(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    def get(path: String, remainingRedirects: Int): Future[Response] = {
      if (remainingRedirects == 0) {
        Future.failed(new RuntimeException("Too many redirects"))
      } else {
        HttpClient.get(path, env, withOverrides(headers)) flatMap {
          case res if res.status >= 300 && res.status <= 399 =>
            get(res.headers("Location").head, remainingRedirects - 1)
          case res => Future(res)
        }
      }
    }
    get(path, remainingRedirects = 10)
  }

  def put(path: String, body: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.put(path, env, body, withOverrides(headers))
  }

  def delete(path: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.delete(path, env, withOverrides(headers))
  }

  def options(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.options(path, env, withOverrides(headers))
  }

  private def withOverrides(headers: Seq[(String, String)])(implicit env: Env): Seq[(String, String)] = {
    headers :+ headers.find(_._1 == "Config-Override").getOrElse(configOverrideHeader(env)) :+ secretKeyHeader(env)
  }

  private def addOverrides(headers: Map[String, Seq[String]])(implicit env: Env): Map[String, Seq[String]] = {
    val confOverride = "Config-Override" -> Seq(env.map { case (key, value) => s"$key=$value" } .toSeq.mkString(" "))
    val secretKey = "Secret-Key" -> Seq(TestApp.secretKey)
    (headers.toSeq :+ headers.toSeq.find(_._1 == "Config-Override").getOrElse(confOverride) :+ secretKey).toMap[String, Seq[String]]
  }

  private def configOverrideHeader(env: Env): (String, String) = {
    "Config-Override" -> env.map { case (key, value) => s"$key=$value" } .toSeq.mkString(" ")
  }

  private def secretKeyHeader(env: Env): (String, String) = {
    "Secret-Key" -> TestApp.secretKey
  }
}
