package duesoldi.httpclient

import com.ning.http.client.Response
import dispatch.url
import duesoldi.testapp.TestServer
import dispatch.Http

import scala.concurrent.{ExecutionContext, Future}

object HttpClient {

  def get(path: String, server: TestServer)(implicit ec: ExecutionContext): Future[Response] = {
    Http(url(s"http://localhost:${server.port}$path"))
  }

}
