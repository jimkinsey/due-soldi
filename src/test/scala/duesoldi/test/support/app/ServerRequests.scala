package duesoldi.test.support.app

import duesoldi.Env
import duesoldi.test.support.httpclient.HttpClient
import duesoldi.test.support.httpclient.HttpClient.Response

import scala.concurrent.{ExecutionContext, Future}

object ServerRequests
{
  def get(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.get(path, env, withOverrides(headers))
  }

  def put(path: String, body: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.put(path, env, body, withOverrides(headers))
  }

  def delete(path: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.delete(path, env, withOverrides(headers))
  }

  private def withOverrides(headers: Seq[(String, String)])(implicit env: Env): Seq[(String, String)] = {
    headers :+ headers.find(_._1 == "Config-Override").getOrElse(configOverrideHeader(env)) :+ secretKeyHeader(env)
  }

  private def configOverrideHeader(env: Env): (String, String) = {
    "Config-Override" -> env.map { case (key, value) => s"$key=$value" } .toSeq.mkString(" ")
  }

  private def secretKeyHeader(env: Env): (String, String) = {
    "Secret-Key" -> TestApp.secretKey
  }
}
