package sommelier.routing

import ratatoskr.RequestBuilding._
import ratatoskr.ResponseBuilding.ResponseBuilder
import ratatoskr.{Method, Request, Response}
import sommelier.Context
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
  lazy val OPTIONS = RequestMatcher(method = Some(MethodMatcher(Method.OPTIONS)))

  val AnyRequest = RequestMatcher()

  implicit class RouteMaker(matcher: RequestMatcher) { // fixme reconcile with controllers
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def methodToRequest(method: Method): Request = Request(method, "/")

  implicit def statusToResponse(status: Int): Response = Response(status = status)

  implicit def responseToResult(response: Response): Result[Response] = Accepted(response)

  implicit def responseBuilderToResult(builder: ResponseBuilder): Result[Response] = Accepted(builder.response)

  implicit def responseToRejection(response: Response): Rejection = Rejection(response)

  implicit def requestToResult(request: Request): Result[Request] = Accepted(request)

  implicit def statusToResult(status: Int): Result[Response] = Accepted(Response(status))

  implicit def statusToRejection(status: Int): Rejection = Rejection(status)

  implicit def futureResponseToResult(fResponse: Future[Response])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResponse.map(Accepted(_)))

  implicit def futureResponseBuilderToResult(fBuilder: Future[ResponseBuilder])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fBuilder.map(Accepted(_)))

  implicit def futureRequestToResult(fRequest: Future[Request])(implicit executionContext: ExecutionContext): Result[Request] = AsyncResult(fRequest.map(Accepted(_)))

  implicit def futureResultToResult(fResult: Future[Result[Response]])(implicit executionContext: ExecutionContext): Result[Response] = AsyncResult(fResult)

  def reject(response: Response): Result[Response] = Rejected(Rejection(response))

  def rejectRequest(response: Response): Result[Request] = Rejected(Rejection(response))

  implicit class RequestOps(request: Request)
  {
    def basicAuth(auth: Basic): Request = {
      request.header("Authorization" -> s"Basic ${auth.encoded}")
    }
  }
}
