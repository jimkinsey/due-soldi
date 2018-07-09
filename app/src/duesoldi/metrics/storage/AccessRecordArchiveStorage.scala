package duesoldi.metrics.storage

import dearboy.EventBus
import duesoldi.metrics.rendering.MakeAccessCsv
import duesoldi.metrics.storage.AccessRecordArchiveStorage.Event.{ArchiveFailure, ArchiveSuccess}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

object AccessRecordArchiveStorage
{
  def autoArchive(
    events: EventBus,
    threshold: Int,
    getLogSize: GetAccessRecordLogSize,
    getAccessRecordsToArchive: GetAccessRecordsWithCount,
    deleteAccessRecord: DeleteAccessRecord,
    storeArchive: StoreAccessRecordArchive,
    accessCsv: MakeAccessCsv)(implicit executionContext: ExecutionContext): Future[Unit] = {

    val archive =
      for {
        size <- getLogSize() if size > threshold
        records <- getAccessRecordsToArchive(threshold)
        newArchive = accessCsv(records)
        archiveRange = (records.head.time, records.last.time)
        _ <- storeArchive(archiveRange, newArchive)
        _ <- Future.sequence(records.map(deleteAccessRecord))
      } yield { }

    archive.onComplete {
      case Failure(throwable) => events publish ArchiveFailure(throwable)
      case _ => events publish ArchiveSuccess(threshold)
    }

    archive

  }

  sealed trait Event
  object Event
  {
    case class ArchiveSuccess(count: Int) extends Event
    case class ArchiveFailure(throwable: Throwable) extends Event
  }
}
