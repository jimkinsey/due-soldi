package duesoldi

import scala.concurrent.{ExecutionContext, Future}

sealed trait Setup {
  type Setup
  type Teardown
  def setup(env: Env): Setup
  def tearDown: Teardown
}

trait AsyncSetup extends Setup {
  type Setup = Future[Env]
  type Teardown = Future[Unit]
  def setup(env: Env): Future[Env]
  def tearDown: Future[Unit] = Future.successful({})
}

trait SyncSetup extends Setup {
  type Setup = Env
  type Teardown = Unit
  def setup(env: Env): Env
  def tearDown: Unit = {}
}

object Setup {

  def withSetup[T](steps: Setup*)(block: Env => Future[T])(implicit executionContext: ExecutionContext): Future[T] = {

    def setup(env: Env, remainingSteps: Seq[Setup]): Future[Env] = {
      remainingSteps.headOption match {
        case Some(step: SyncSetup) => setup(env ++ step.setup(env), remainingSteps.tail)
        case Some(step: AsyncSetup) => step.setup(env).flatMap( e => setup(env ++ e, remainingSteps.tail) )
        case None => Future.successful(env)
      }
    }

    lazy val tearDown = Future.sequence(steps.map {
      case step: SyncSetup => Future.successful(step.tearDown)
      case step: AsyncSetup => step.tearDown
    })

    for {
      env <- setup(Map("ADMIN_CREDENTIALS" -> "user:password"), steps)
      res <- block(env)
      _   <- tearDown
    } yield {
      res
    }
  }

}
