package duesoldi.httpclient

import dispatch.url
import duesoldi.testapp.TestServer
import dispatch.Http

import scala.concurrent.{ExecutionContext, Future}

object HttpClient {

  case class Response(body: String)

  def get(path: String, server: TestServer)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${server.port}$path")).map { res =>
      Response(res.getResponseBody)
    }
  }

}
