import scala.concurrent.Future
import scala.util.Failure

type Env = Map[String, String]

import scala.concurrent.ExecutionContext.Implicits._

//def add(vars: (String, String)*): Future[Env] = Future { vars.toMap }
//
//def withSetup[T](setup: (Future[Env])*)(block: Env => Future[T]) = {
//  Future.foldLeft[Env, Env]((setup).to[collection.immutable.Iterable])(Map[String, String]())(_ ++ _) flatMap block
//}

trait Setup {
  def setup: Future[Env]
  def tearDown: Future[Unit]
}

def withSetup[T](steps: Setup*)(block: Env => Future[T]): Future[T] = {
  lazy val setup = Future.foldLeft[Env, Env](steps.map(_.setup).to[collection.immutable.Iterable])(Map[String, String]())(_ ++ _)
  lazy val tearDown = Future.sequence(steps.map(_.tearDown))

  val res: Future[T] = for {
    env <- setup
    res <- block(env)
    _   <- tearDown
  } yield {
    res
  }

  res
}

def add(vars: (String, String)*) = new Setup {
  override def setup: Future[Env] = Future.successful(vars.toMap)
  override def tearDown: Future[Unit] = Future.successful(println("Tearing down!"))
}



// FIXME this needs to be setup AND teardown
// not seq future of env, but seq of some kind of setup/teardown object

val fut2 = withSetup(
  add("a" -> "A"),
  add("b" -> "B")
) { e =>
  Future { assert(e.keySet == Set("a", "b")) }
}

fut2.onComplete {
  case Failure(ex) => ex.printStackTrace()
}

Thread.sleep(100)
fut2

