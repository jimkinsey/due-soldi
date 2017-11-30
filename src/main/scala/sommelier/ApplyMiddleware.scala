package sommelier

import sommelier.Middleware.Incoming

object ApplyMiddleware
{
  def incoming(middleware: Seq[Middleware])(request: Request): Result[Request] = {
    middleware.foldLeft[Result[Request]](SyncResult.Accepted(request)) {
      case (acc, Incoming(matcher, handle)) =>
        acc.flatMap {
          case req if matcher.rejects(req).isEmpty => handle(req)
          case req => acc
        }
      case (acc, _) => acc
    }
  }


}
