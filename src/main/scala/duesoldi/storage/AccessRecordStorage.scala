package duesoldi.storage

import duesoldi.events.Events
import duesoldi.storage.AccessRecordStorage.Event.{RecordFailure, RecordSuccess}
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

object AccessRecordStorage
{
  type StoreAccessRecord = (Access) => Future[Unit]

  def enable(events: Events, store: StoreAccessRecord)(implicit executionContext: ExecutionContext) {
    events.respondTo {
      case access: Access => store(access).onComplete {
        case Failure(ex) =>
          events.emit(RecordFailure(ex))
        case _ =>
          events.emit(RecordSuccess)
      }
    }
  }

  sealed trait Event
  object Event {
    case class RecordFailure(throwable: Throwable) extends Event
    case object RecordSuccess extends Event
  }
}
