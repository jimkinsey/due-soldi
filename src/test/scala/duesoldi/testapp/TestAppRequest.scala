package duesoldi.testapp

import com.ning.http.client.Response
import duesoldi.httpclient.HttpClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by jimkinsey on 11/01/17.
  */
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
