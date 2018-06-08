package sommelier.routing

import ratatoskr.Request
import sommelier.Response
import sommelier.routing.Middleware.{Incoming, Outgoing}

private[sommelier] object ApplyMiddleware
{
  def applyIncoming(middleware: Seq[Middleware])(incoming: Request): Result[Request] = {
    middleware.foldLeft[Result[Request]](SyncResult.Accepted(incoming)) {
      case (acc, Incoming(matcher, handle)) =>
        acc.flatMap {
          case request if matcher.rejects(request).isEmpty => handle(request)
          case _ => acc
        }
      case (acc, _) => acc
    }
  }

  def applyOutgoing(middleware: Seq[Middleware])(incoming: Request, outgoing: Response): Result[Response] = {
    middleware.foldLeft[Result[Response]](SyncResult.Accepted(outgoing)) {
      case (acc, Outgoing(matcher, handle)) =>
        acc.flatMap {
          case response if matcher.rejects(incoming).isEmpty => handle(incoming, response)
          case _ => acc
        }
      case (acc, _) => acc
    }
  }
}
