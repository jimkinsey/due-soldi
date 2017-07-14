package duesoldi.testapp

import duesoldi.{Server, _}

import scala.concurrent.{ExecutionContext, Future}

object ServerSupport {

  def withServer[T](block: Server => Future[T])(implicit executionContext: ExecutionContext): Env => Future[T] = (env: Env) => {
    def doTest(server: Server) = {
      val test = block(server)
      test.onComplete( _ => server.stop() )
      test
    }

    for {
      server <- TestApp.start(env)
      res    <- doTest(server)
    } yield {
      res
    }
  }

}
