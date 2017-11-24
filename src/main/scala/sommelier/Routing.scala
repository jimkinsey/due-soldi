package sommelier

object Routing
{
  type Handler = (Context => Response)

  lazy val GET = RequestMatcher(method = MethodMatcher(Method.GET))

  implicit class RouteMaker(matcher: RequestMatcher) {
    def respond(handler: Handler) = Route(matcher, handler)
  }

  implicit def statusToResponse(status: Int): Response = Response(status = status)
}
