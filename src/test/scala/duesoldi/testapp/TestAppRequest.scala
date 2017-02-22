package duesoldi.testapp

import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response
import duesoldi.{Env, Server}

import scala.concurrent.{ExecutionContext, Future}

object TestAppRequest {

  def get[A](path: String, headers: (String, String)*)(handle: (Response => A))(implicit ec: ExecutionContext): Env => Future[A] = {
    (env: Env) =>
      for {
        server <- TestApp.start(env)
        res    <- HttpClient.get(path, server, headers) // FIXME no passing server here
        _      <- server.stop()
      } yield {
        handle(res)
      }
  }

  def getRaw(path: String, headers: (String, String)*)(implicit ec: ExecutionContext, server: Server): Future[Response] = {
    HttpClient.get(path, server, headers)
  }

}
