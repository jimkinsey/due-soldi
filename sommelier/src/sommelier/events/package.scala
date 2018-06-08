package sommelier

import scala.concurrent.duration.Duration

package object events
{
  case class ExceptionWhileRouting(request: ratatoskr.Request, exception: Throwable)
  case class Completed(request: ratatoskr.Request, response: Response, duration: Duration)
  case object HaltRequested
}
