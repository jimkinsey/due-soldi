package duesoldi.metrics.storage

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import dearboy.EventBus
import duesoldi.metrics.storage.AccessRecordStorage.Event.{RecordFailure, RecordSuccess}
import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext
import scala.util.Failure

object AccessRecordStorage
{
  def enable(events: EventBus, store: StoreAccessRecord)(implicit executionContext: ExecutionContext) {
    events.subscribe {
      case access: Access => store(access).onComplete {
        case Failure(ex) =>
          events.publish(RecordFailure(ex))
        case _ =>
          events.publish(RecordSuccess)
      }
    }
  }

  sealed trait Event
  object Event
  {
    case class RecordFailure(throwable: Throwable) extends Event
    case object RecordSuccess extends Event
  }

  def fixMissingIds(getAccessRecords: GetAccessRecords, updateAccessRecord: UpdateAccessRecord)
                   (implicit executionContext: ExecutionContext) {
    for {
      records <- getAccessRecords(ZonedDateTime.of(1978,7, 20, 20, 10, 0, 0, ZoneId.of("UTC")))
    } yield {
      records.collect { case record if record.id.isEmpty =>
        updateAccessRecord(record.copy(id = UUID.randomUUID().toString))
      }
    }
  }
}
