package sommelier.routing

import sommelier.handling.Context
import sommelier.messaging.{Request, Response}
import sommelier.routing.AuthorizationFailed.{Forbidden, Unauthorized}
import sommelier.routing.SyncResult.Rejected

private[sommelier] object ApplyRoutes
{
  def applyRoutes(routes: Seq[Route])(request: Request): Result[Response] = {
    routes.toStream.map { route =>
      route -> route.matcher.rejects(request)
    } match {
      case FirstMatching(route) => route.handle(Context(request, route.matcher))
      case ClosestMatching(rejection) => Rejected(rejection)
      case _ => Rejected(Rejection(Response(404)))
    }
  }

  object FirstMatching
  {
    def unapply(routeResults: Seq[(Route,Option[Rejection])]): Option[Route] = {
      routeResults.find(_._2.isEmpty).map(_._1)
    }
  }

  object ClosestMatching
  {
    def unapply(routeResults: Seq[(Route, Option[Rejection])]): Option[Rejection] = {
      routeResults.flatMap(_._2).sortBy(priority).lastOption // really not sure about this!
    }
  }

  def priority(rejection: Rejection): Int = {
    rejection match {
      case ResourceNotFound => 1
      case MethodNotAllowed => 2
      case Unacceptable => 3
      case Unauthorized(_) => 4
      case Forbidden => 5
    }
  }
}
