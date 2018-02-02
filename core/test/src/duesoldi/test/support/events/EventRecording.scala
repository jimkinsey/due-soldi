package duesoldi.test.support.events

import scala.collection.mutable

object EventRecording
{
  def withRecorder[T](block: EventRecorder => T) = {
    block(new EventRecorder)
  }

  class EventRecorder
  {
    def emit(event: Any): Unit = events.append(event)
    def received(event: Any): Boolean = events.contains(event)
    private lazy val events: mutable.Buffer[Any] = mutable.Buffer.empty
  }
}
