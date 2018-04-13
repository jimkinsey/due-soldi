package sommelier.routing

import sommelier.Context
import sommelier.messaging.{Method, Request, Response}
import sommelier.routing.SyncResult.{Accepted, Rejected}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object Routing
{
  type Handler = (Context => Result[Response]) // fixme does this belong here?

  lazy val GET = RequestMatcher(method = Some(MethodMatcher(Method.GET)))
  lazy val POST = RequestMatcher(method = Some(MethodMatcher(Method.POST)))
  lazy val PUT = RequestMatcher(method = Some(MethodMatcher(Method.PUT)))
  lazy val DELETE = RequestMatcher(method = Some(MethodMatcher(Method.DELETE)))

  val AnyRequest = RequestMatcher()

  implicit class RouteMaker(matcher: RequestMatcher) { // fixme reconcile with controllers
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def statusToResponse(status: Int): Response = Response(status = status)

  implicit def responseToResult(response: Response): Result[Response] = Accepted(response)

  implicit def responseToRejection(response: Response): Rejection = Rejection(response)

  implicit def requestToResult(request: Request): Result[Request] = Accepted(request)

  implicit def statusToResult(status: Int): Result[Response] = Accepted(Response(status))

  implicit def statusToRejection(status: Int): Rejection = Rejection(status)

  implicit def futureResponseToResult(fResponse: Future[Response])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResponse.map(Accepted(_)))

  implicit def futureRequestToResult(fRequest: Future[Request])(implicit executionContext: ExecutionContext): Result[Request] = AsyncResult(fRequest.map(Accepted(_)))

  implicit def futureResultToResult(fResult: Future[Result[Response]])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResult)

  def reject(response: Response): Result[Response] = Rejected(Rejection(response))

  def rejectRequest(response: Response): Result[Request] = Rejected(Rejection(response))
}
