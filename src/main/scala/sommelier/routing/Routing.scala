package sommelier.routing

import SyncResult.{Accepted, Rejected}
import sommelier.messaging.{Method, Request, Response}
import sommelier.{Context, Rejection, Route}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object Routing
{
  type Handler = (Context => Result[Response])

  lazy val GET = RequestMatcher(method = Some(MethodMatcher(Method.GET)))
  lazy val POST = RequestMatcher(method = Some(MethodMatcher(Method.POST)))
  lazy val PUT = RequestMatcher(method = Some(MethodMatcher(Method.PUT)))
  lazy val DELETE = RequestMatcher(method = Some(MethodMatcher(Method.DELETE)))

  val AnyRequest = RequestMatcher()

  implicit class RouteMaker(matcher: RequestMatcher) {
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def statusToResponse(status: Int): Response = Response(status = status)

  implicit class OptionRejection[T](opt: Option[T])
  {
    def rejectWith(response: => Response): SyncResult[T] =
      opt.fold[SyncResult[T]](Rejected(Rejection(response)))(Accepted(_))
  }

  implicit class AsyncOptionRejection[T](fOpt: Future[Option[T]])
  {
    def rejectWith(response: => Response)(implicit executionContext: ExecutionContext): AsyncResult[T] =
      AsyncResult(fOpt.map(_ rejectWith response))
  }

  implicit class EitherRejection[L,R](either: Either[L,R])
  {
    def rejectWith(ifLeft: L => Response): SyncResult[R] =
      either.fold[SyncResult[R]](left => Rejected(Rejection(ifLeft(left))), Accepted(_))
  }

  implicit class AsyncEitherRejection[L,R](fEither: Future[Either[L,R]])
  {
    def rejectWith(ifLeft: L => Response)(implicit executionContext: ExecutionContext): AsyncResult[R] =
      AsyncResult(fEither.map(_ rejectWith ifLeft))
  }

  implicit def responseToResult(response: Response): Result[Response] = Accepted(response)

  implicit def responseToRejection(response: Response): Rejection = Rejection(response)

  implicit def requestToResult(request: Request): Result[Request] = Accepted(request)

  implicit def statusToResult(status: Int): Result[Response] = Accepted(Response(status))

  implicit def futureResponseToResult(fResponse: Future[Response])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResponse.map(Accepted(_)))

  implicit def futureRequestToResult(fRequest: Future[Request])(implicit executionContext: ExecutionContext): Result[Request] = AsyncResult(fRequest.map(Accepted(_)))

  implicit def futureResultToResult(fResult: Future[Result[Response]])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResult)

  def reject(response: Response): Result[Response] = Rejected(Rejection(response))

  def rejectRequest(response: Response): Result[Request] = Rejected(Rejection(response))
}
