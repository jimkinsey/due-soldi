package sommelier

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object Routing
{
  type Handler = (Context => Result[Response])

  lazy val GET = RequestMatcher(method = Some(MethodMatcher(Method.GET)))
  lazy val POST = RequestMatcher(method = Some(MethodMatcher(Method.POST)))
  lazy val PUT = RequestMatcher(method = Some(MethodMatcher(Method.PUT)))
  lazy val DELETE = RequestMatcher(method = Some(MethodMatcher(Method.DELETE)))

  implicit class RouteMaker(matcher: RequestMatcher) {
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def statusToResponse(status: Int): Response = Response(status = status)

  implicit class OptionRejection[T](opt: Option[T])
  {
    def rejectWith(response: => Response): SyncResult[T] = opt.fold[SyncResult[T]](SyncResult.Rejected(Rejection(response)))(SyncResult.Accepted(_))
  }

  implicit class AsyncOptionRejection[T](fOpt: Future[Option[T]])
  {
    def rejectWith(response: => Response)(implicit executionContext: ExecutionContext): AsyncResult[T] = AsyncResult(fOpt.map(_ rejectWith response))
  }

  implicit def responseToResult(response: Response): Result[Response] = SyncResult.Accepted(response)

  implicit def statusToResult(status: Int): Result[Response] = SyncResult.Accepted(Response(status))

  implicit def futureResponseToResult(fResponse: Future[Response])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResponse.map(SyncResult.Accepted(_)))

  implicit def futureResultToResult(fResult: Future[Result[Response]])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResult)

  def reject(response: Response): Result[Response] = SyncResult.Rejected(Rejection(response))
}
