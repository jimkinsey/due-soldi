package duesoldi

import scala.concurrent.{ExecutionContext, Future}

trait Setup {
  def setup: Future[Env]
  def tearDown: Future[Unit]
}

object Setup {

  def withSetup[T](steps: Setup*)(block: Env => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    lazy val setup = Future.foldLeft[Env, Env](steps.map(_.setup).to[collection.immutable.Iterable])(Map[String, String]())(_ ++ _)
    lazy val tearDown = Future.sequence(steps.map(_.tearDown))

    for {
      env <- setup
      res <- block(env)
      _   <- tearDown
    } yield {
      res
    }
  }

}
