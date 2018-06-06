import ratatoskr.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

package object cicerone {
  sealed trait Failure
  case object ConnectionFailure extends Failure
  case object MalformedURL extends Failure
  case class UnexpectedException(exception: Exception) extends Failure

  type Headers = Map[String,Seq[String]]

  val http = RequestBuilder()

  implicit def builtRequest(builder: RequestBuilder): Request = builder.build

  implicit class RequestSender(builder: RequestBuilder)
  {
    def send(implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = {
      new Client().send(builder.build)
    }
  }
}
