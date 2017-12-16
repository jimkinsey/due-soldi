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
}
