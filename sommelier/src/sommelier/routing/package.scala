package sommelier

import sommelier.routing.Middleware.{Incoming, Outgoing}
import ratatoskr.Request

package object routing
{
  case class Route(matcher: RequestMatcher, handle: Routing.Handler)

  implicit class MiddlewareMaker(matcher: RequestMatcher)
  {
    def incoming(handle: ratatoskr.Request => Result[ratatoskr.Request]): Middleware = Incoming(matcher, handle)
    def outgoing(handle: (ratatoskr.Request, Response) => Result[Response]): Middleware = Outgoing(matcher, handle)
  }
}
