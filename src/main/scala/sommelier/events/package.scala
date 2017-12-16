package sommelier

import scala.concurrent.duration.Duration

package object events
{
  type Subscriber = PartialFunction[Any, Unit]

  class EventBus
  {
    def publish(event: Any) {
      subscribers.map(_.lift(event))
    }
    def subscribe(subscriber: Subscriber) {
      subscribers.append(subscriber)
    }
    private lazy val subscribers: collection.mutable.Buffer[Subscriber] = collection.mutable.Buffer.empty
  }

  case class ExceptionWhileRouting(request: Request, exception: Throwable)
  case class Completed(request: Request, response: Response, duration: Duration)
  case object HaltRequested
}
