package duesoldi.testapp

import duesoldi.Env
import duesoldi.httpclient.HttpClient
import duesoldi.httpclient.HttpClient.Response
import duesoldi.storage.FilesystemMarkdownSource

import scala.concurrent.{ExecutionContext, Future}

object TestAppRequest {

  private lazy val defaultBlogStoreConfig = new FilesystemMarkdownSource.Config {
    override def path: String = "/tmp/blog/default"
  }

  def get[A](path: String)(handle: (Response => A))(implicit ec: ExecutionContext, storeConfig: FilesystemMarkdownSource.Config = defaultBlogStoreConfig): Env => Future[A] = {
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
