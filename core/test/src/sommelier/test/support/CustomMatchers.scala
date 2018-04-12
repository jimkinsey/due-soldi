package sommelier.test.support

import sommelier.Response
import sommelier.routing.SyncResult.{Accepted, Rejected}
import sommelier.routing.{AsyncResult, Result}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object CustomMatchers
{
  implicit class ResultMatchers[T](result: Result[T])
  {
    def isRejectionAs(response: Response): Boolean = result match {
      case Rejected(rejection) if rejection.response == response => true
      case _ => false
    }
    def isAcceptedWhere(pred: T => Boolean): Boolean = result match {
      case Accepted(value) if pred(value) => true
      case AsyncResult(result) => Await.result(result, Duration.create("5 seconds")) isAcceptedWhere pred
      case _ => false
    }
  }
  implicit class EitherAssertions[L,R](either: Either[L,R])
  {
    def isLeftOf(value: L): Boolean = either.left.toOption.contains(value)
    def isRightOf(value: R): Boolean = either.toOption.contains(value)
    def isRightWhere(pred: R => Boolean): Boolean = either.toOption.exists(pred)
  }
}

object StreamHelpers
{
  implicit class ByteStreamHelper(stream: Stream[Byte])
  {
    def asString: String = new String(stream.toArray, "UTF-8")
  }
}