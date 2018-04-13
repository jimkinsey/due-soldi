package sommelier

import scala.concurrent.duration.Duration

package object events
{
  case class ExceptionWhileRouting(request: Request, exception: Throwable)
  case class Completed(request: Request, response: Response, duration: Duration)
  case object HaltRequested
}
