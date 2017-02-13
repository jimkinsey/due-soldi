package duesoldi.testapp

import duesoldi.Env
import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response

import scala.concurrent.{ExecutionContext, Future}

object TestAppRequest {

  def get[A](path: String)(handle: (Response => A))(implicit ec: ExecutionContext): Env => Future[A] = {
    (env: Env) =>
      for {
        server <- TestApp.start(ec, env)
        res    <- HttpClient.get(path, server)
        _      <- TestApp stop server
      } yield {
        handle(res)
      }
  }

}
