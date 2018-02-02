package sommelier.routing

import sommelier.routing.Middleware.{Incoming, IncomingHandler, Outgoing, OutgoingHandler}
import sommelier.routing.Routing.Handler

import scala.collection.mutable

trait Controller
{
  val routes: mutable.Buffer[Route] = mutable.Buffer.empty
  val middleware: mutable.Buffer[Middleware] = mutable.Buffer.empty

  implicit class RouteRegistration(requestMatcher: RequestMatcher)
  {
    def >--(handler: IncomingHandler): Unit = middleware.append(Incoming(requestMatcher, handler))
    def ->-(handler: Handler): Unit = routes.append(Route(requestMatcher, handler))
    def -->(handler: OutgoingHandler): Unit = middleware.append(Outgoing(requestMatcher, handler))
  }
}
