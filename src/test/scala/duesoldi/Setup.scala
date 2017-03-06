package duesoldi

import scala.concurrent.{ExecutionContext, Future}

trait Setup {
  def setup(env: Env): Future[Env]
  def tearDown: Future[Unit] = Future.successful({})
}

object Setup {

  def withSetup[T](steps: Setup*)(block: Env => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
//    lazy val setup = Future.foldLeft[Env, Env](steps.map(_.setup).to[collection.immutable.Iterable])(Map[String, String]())(_ ++ _)

    def setup(env: Env, remainingSteps: Seq[Setup]): Future[Env] = {
      remainingSteps.headOption match {
        case Some(step) => step.setup(env).flatMap( e => setup(env ++ e, remainingSteps.tail) )
        case None       => Future.successful(env)
      }
    }

    lazy val tearDown = Future.sequence(steps.map(_.tearDown))

    for {
      env <- setup(Map.empty, steps)
      res <- block(env)
      _   <- tearDown
    } yield {
      res
    }
  }

}
