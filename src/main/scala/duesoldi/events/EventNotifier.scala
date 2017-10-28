package duesoldi.events

import duesoldi.events.Events.Responder

import scala.collection.mutable

object Events {
  type Responder = PartialFunction[Any, Unit]
}

class Events {
  def emit(event: Any): Unit = { responders collect { case responder if responder.isDefinedAt(event) => responder.apply(event) } }
  def respondTo(responder: Responder): Unit = responders.append(responder)

  private lazy val responders: mutable.Buffer[Responder] = mutable.Buffer.empty
}