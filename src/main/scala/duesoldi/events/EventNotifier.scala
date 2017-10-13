package duesoldi.events

import duesoldi.events.Events.Responder

import scala.collection.mutable

trait Event

object Events {
  type Responder = PartialFunction[Event, Unit]
}

class Events {
  def emit(event: Event): Unit = { responders collect { case responder if responder.isDefinedAt(event) => responder.apply(event) } }
  def respondTo[T <: Event](responder: Responder): Unit = responders.append(responder)

  private lazy val responders: mutable.Buffer[Responder] = mutable.Buffer.empty
}