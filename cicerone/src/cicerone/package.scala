import ratatoskr.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

package object cicerone
{
  type Result = Future[Either[Failure, Response]]

  sealed trait Failure
  case object ConnectionFailure extends Failure
  case object MalformedURL extends Failure
  case class UnexpectedException(exception: Exception) extends Failure

  type Headers = Map[String,Seq[String]]

  def http(request: Request)(implicit executionContext: ExecutionContext): Result = new Client().send(request)
}
