package sommelier

import sommelier.routing.Middleware.{Incoming, Outgoing}

package object routing
{
  case class Route(matcher: RequestMatcher, handle: Routing.Handler)

  implicit class MiddlewareMaker(matcher: RequestMatcher)
  {
    def incoming(handle: Request => Result[Request]): Middleware = Incoming(matcher, handle)
    def outgoing(handle: (Request, Response) => Result[Response]): Middleware = Outgoing(matcher, handle)
  }
}
