package duesoldi.metrics.storage

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import dearboy.EventBus
import duesoldi.metrics.rendering.AccessCsv
import duesoldi.metrics.storage.AccessRecordArchiveStore.Archive
import duesoldi.metrics.storage.AccessRecordStorage.Event.{RecordFailure, RecordSuccess}
import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext
import scala.util.Failure

object AccessRecordStorage
{
  def getIncludingArchived(getAccessRecords: GetAccessRecords, getAccessRecordArchive: GetAccessRecordArchive)
                          (implicit executionContext: ExecutionContext): GetAllAccessRecords = (start: ZonedDateTime) => {
    for {
      newRecords <- getAccessRecords(start)

      _ = println(s"DEBUG Got ${newRecords.size} new access records")

      archives <- getAccessRecordArchive(start)

      _ = println(s"DEBUG Got ${archives.size} archives since ${start.format(DateTimeFormatter.ISO_DATE_TIME)}")

      archiveFiles = archives.collect { case Archive(_, _, csv) => csv }
      archiveRecords = archiveFiles.map(AccessCsv.parse).collect {
        case Right(records) => records.filter(_.time.isAfter(start))
      } flatten

      _ = println(s"DEBUG Total records from archive = ${archiveRecords.size}")
    } yield {
      // TODO propagate failure when one of the archives can't be parsed
      Right(newRecords ++ archiveRecords)
    }
  }

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
}
