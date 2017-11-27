package sommelier

import scala.language.implicitConversions

object Routing
{
  type Handler = (Context => Result)

  lazy val GET = RequestMatcher(method = Some(MethodMatcher(Method.GET)))
  lazy val POST = RequestMatcher(method = Some(MethodMatcher(Method.POST)))
  lazy val PUT = RequestMatcher(method = Some(MethodMatcher(Method.PUT)))
  lazy val DELETE = RequestMatcher(method = Some(MethodMatcher(Method.DELETE)))

  implicit class RouteMaker(matcher: RequestMatcher) {
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def statusToResponse(status: Int): Response = Response(status = status)

  implicit class OptionRejection[T](opt: Option[T]) {
    def rejectWith(response: => Response): Either[Rejection,T] = opt.toRight({ Rejection(response) })
  }

  implicit def responseToResult(response: Response): Result = Right(response)

  implicit def statusToResult(status: Int): Result = Right(Response(status))
}
