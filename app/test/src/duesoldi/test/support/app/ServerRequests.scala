package duesoldi.test.support.app

import duesoldi.Env
import duesoldi.test.support.httpclient.HttpClient
import ratatoskr.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

object ServerRequests
{
  def send(vanilla: Request)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    val environmentalised = vanilla.copy(url = s"http://localhost:${env("PORT")}${vanilla.url}", headers = addOverrides(vanilla.headers))
    cicerone.http(environmentalised).map {
      case Right(response) => response
      case Left(cicerone.UnexpectedException(exception)) => throw exception
      case Left(failure) => throw new RuntimeException(s"HTTP request failed unexpectedly [$failure]")
    }
  }

  def get(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.get(path, env, withOverrides(headers))
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
