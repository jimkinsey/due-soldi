package object dearboy
{
  type Subscriber = PartialFunction[Any, Unit]
  type Publish = Any => Unit
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
}
