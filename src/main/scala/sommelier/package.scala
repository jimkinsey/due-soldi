import sommelier.Middleware.{Incoming, Outgoing}
import sommelier.routing.{RequestMatcher, Result, Routing}

package object sommelier
{
  case class Context(request: Request, matcher: RequestMatcher)

  type Request = messaging.Request
  type Response = messaging.Response

  trait Rejection
  {
    def response: Response
  }
  object Rejection
  {
    def apply(r: Response) = new Rejection {
      override def response: Response = r
    }
  }

  case class Route(matcher: RequestMatcher, handle: Routing.Handler)

  sealed trait Middleware
  object Middleware
  {
    type IncomingHandler = Request => Result[Request]
    type OutgoingHandler = (Request, Response) => Result[Response]
    case class Incoming(matcher: RequestMatcher, handle: IncomingHandler) extends Middleware
    case class Outgoing(matcher: RequestMatcher, handle: OutgoingHandler) extends Middleware
  }

  implicit class MiddlewareMaker(matcher: RequestMatcher)
  {
    def incoming(handle: Request => Result[Request]): Middleware = Incoming(matcher, handle)
    def outgoing(handle: (Request, Response) => Result[Response]): Middleware = Outgoing(matcher, handle)
  }
}

// todo more tests
// todo restructure - DSL on sommelier package object, implementation stuff in packages
// todo stress testing
// todo events = dearboy, injection = beefish
// todo why 405 instead of 404 on so many paths?