package sommelier

import scala.concurrent.{ExecutionContext, Future}

sealed trait Result[+T]
{
  def flatMap[T1](fn: T => Result[T1]): Result[T1]
  def map[T1](fn: T => T1): Result[T1]
  def recover[T1 >: T](fn: Rejection => T1): Result[T1]
}

sealed trait SyncResult[T]
extends Result[T]

object SyncResult
{
  case class Rejected[T](rejection: Rejection)
  extends SyncResult[T]
  {
    override def flatMap[T1](fn: (T) => Result[T1]): Result[T1] = Rejected(rejection)
    override def map[T1](fn: (T) => T1): Result[T1] = Rejected(rejection)
    override def recover[T1](fn: (Rejection) => T1): Result[T1] = Accepted(fn(rejection))
  }
  case class Accepted[T](result: T)
  extends SyncResult[T]
  {
    override def flatMap[T1](fn: (T) => Result[T1]): Result[T1] = fn(result)
    override def map[T1](fn: (T) => T1): Result[T1] = Accepted(fn(result))
    override def recover[T1 >: T](fn: Rejection => T1): Result[T1] = this
  }
}

case class AsyncResult[T](result: Future[Result[T]])(implicit executionContext: ExecutionContext)
extends Result[T]
{
  override def flatMap[T1](fn: (T) => Result[T1]): Result[T1] =
    AsyncResult(result.flatMap {
      case SyncResult.Rejected(f) => Future.successful(SyncResult.Rejected(f))
      case SyncResult.Accepted(t) => Future.successful(fn(t))
      case AsyncResult(futT1) => futT1.map(_ flatMap fn)
    })
  override def map[T1](fn: (T) => T1): Result[T1] = AsyncResult(result.map(_ map fn))
  override def recover[T1 >: T](fn: (Rejection) => T1) = AsyncResult(result.map(_ recover fn))
}

object Result
{
  implicit class OptionResult[T](opt: Option[T])
  {
    def rejectWith(ifNone: => Rejection): Result[T] = opt.fold[Result[T]](SyncResult.Rejected[T](ifNone))(SyncResult.Accepted[T])
  }
}