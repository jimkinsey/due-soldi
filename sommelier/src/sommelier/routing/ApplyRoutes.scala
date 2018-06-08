package sommelier.routing

import ratatoskr.{Request, Response}
import sommelier.handling.Context
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
    rejection.response.status match {
      case 404 => 1
      case 405 => 2
      case 406 => 3
      case 401 => 4
      case 403 => 5
      case _ => Integer.MAX_VALUE
    }
  }
}
