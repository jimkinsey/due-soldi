package duesoldi.testapp

import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response

import scala.concurrent.{ExecutionContext, Future}

object TestAppRequest {

  def get[A](path: String)(handle: (Response => A))(implicit ec: ExecutionContext): Future[A] = {
    for {
      server <- TestApp.start
      res    <- HttpClient.get(path, server)
      _      <- TestApp stop server
    } yield {
      handle(res)
    }
  }

}
