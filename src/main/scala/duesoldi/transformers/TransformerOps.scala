package duesoldi.transformers

import cats.data.{EitherT, OptionT}

import scala.concurrent.{ExecutionContext, Future}

object TransformerOps {
  import cats.instances.all._

  implicit def toEitherTOps[L,R](either: Either[L,R])(implicit executionContext: ExecutionContext): EitherTOps[L,R] = new EitherTOps[L,R](EitherT.fromEither[Future](either))
  implicit def toEitherTOps[L,R](futureOfEither: Future[Either[L,R]])(implicit executionContext: ExecutionContext): EitherTOps[L,R] = new EitherTOps[L,R](EitherT(futureOfEither))

  implicit class EitherTOps[L,R](val transformer: EitherT[Future, L, R])(implicit executionContext: ExecutionContext) {
    def propagate[LL >: L]: EitherT[Future, LL, R] = transformer.asInstanceOf[EitherT[Future, LL, R]]
    def failWith[LA](ifLeft: L => LA): EitherT[Future, LA, R] = transformer.leftMap(ifLeft)
  }

  implicit def toValue[L,R](transformer: EitherT[Future, L, R]): Future[Either[L,R]] = transformer.value

  implicit def toOptionTOps[T](futureOfOption: Future[Option[T]])(implicit executionContext: ExecutionContext): OptionTOps[T] = new OptionTOps[T](OptionT(futureOfOption))
  implicit def toOptionTOps[T](option: Option[T])(implicit executionContext: ExecutionContext): OptionTOps[T] = new OptionTOps[T](OptionT.fromOption(option))

  implicit class OptionTOps[T](val transformer: OptionT[Future, T])(implicit executionContext: ExecutionContext) {
    def failWith[L](ifNone: => L): EitherT[Future, L, T] = transformer.toRight(ifNone)
  }
}
