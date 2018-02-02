package sommelier.routing

import sommelier.{Request, Response}

sealed trait Middleware
object Middleware
{
  type IncomingHandler = Request => Result[Request]
  type OutgoingHandler = (Request, Response) => Result[Response]
  case class Incoming(matcher: RequestMatcher, handle: IncomingHandler) extends Middleware
  case class Outgoing(matcher: RequestMatcher, handle: OutgoingHandler) extends Middleware
}
