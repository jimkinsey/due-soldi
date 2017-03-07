package duesoldi.testapp

import duesoldi.Server
import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response

import scala.concurrent.{ExecutionContext, Future}

trait ServerRequests {

  def get(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, server: Server): Future[Response] = {
    HttpClient.get(path, server, headers)
  }

  def put(path: String, body: String, headers: (String, String)*)(implicit executionContext: ExecutionContext, server: Server): Future[Response] = {
    HttpClient.put(path, server, body, headers)
  }

}
