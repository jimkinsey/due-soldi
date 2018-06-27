package duesoldi.metrics.storage

import dearboy.EventBus
import duesoldi.metrics.rendering.MakeAccessCsv
import duesoldi.metrics.storage.AccessRecordArchiveStorage.Event.{ArchiveFailure, ArchiveSuccess}
import duesoldi.metrics.storage.AccessRecordStorage.Event.RecordSuccess

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

object AccessRecordArchiveStorage
{
  def enable(
    events: EventBus,
    threshold: Int,
    getLogSize: GetAccessRecordLogSize,
    getAccessRecordsToArchive: GetAccessRecordsWithCount,
    delete: DeleteAccessRecords,
    storeArchive: StoreAccessRecordArchive,
    accessCsv: MakeAccessCsv)(implicit executionContext: ExecutionContext) {
    events.subscribe {
      case RecordSuccess => {
        val archive = for {
          size <- getLogSize() if size > threshold
          records <- getAccessRecordsToArchive(threshold)
          newArchive = accessCsv(records)
          archiveRange = (records.head.time, records.last.time)
          _ <- storeArchive(archiveRange, newArchive)
          _ <- delete(records)
        } yield { }
        archive.onComplete {
          case Failure(throwable) => events publish ArchiveFailure(throwable)
          case _ => events publish ArchiveSuccess(threshold)
        }
      }
    }
  }

  sealed trait Event
  object Event
  {
    case class ArchiveSuccess(count: Int) extends Event
    case class ArchiveFailure(throwable: Throwable) extends Event
  }
}
