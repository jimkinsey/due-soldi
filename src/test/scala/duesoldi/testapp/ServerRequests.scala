package duesoldi.testapp

import duesoldi.Env
import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response

import scala.concurrent.{ExecutionContext, Future}

object ServerRequests {

  def get(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.get(path, env, headers)
  }

  def put(path: String, body: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.put(path, env, body, headers)
  }

  def delete(path: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, env: Env): Future[Response] = {
    HttpClient.delete(path, env, headers)
  }

}