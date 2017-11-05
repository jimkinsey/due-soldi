package duesoldi

import scala.util.{Failure, Success, Try}

package object exceptions
{
  implicit class TryExtensions[T](aTry: Try[T]) {
    def fin(finish: => Try[Unit]): Try[T] = {
      aTry.transform(
        success => finish.map(_ => success),
        _ => finish match {
          case Success(_) => aTry
          case failure: Failure[_] => failure.asInstanceOf[Failure[T]]
        }
      )
    }
  }
}
