package duesoldi.metrics.storage

import java.time.ZonedDateTime
import java.util.UUID

import dearboy.EventBus
import duesoldi.metrics.rendering.{AccessCsv, MakeAccessCsv}
import duesoldi.metrics.storage.AccessRecordArchiveStorage.Event.{ArchiveFailure, ArchiveSuccess}
import duesoldi.metrics.storage.AccessRecordArchiveStore.Archive
import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

object AccessRecordArchiveStorage
{

  def tidyUp(getArchive: GetAccessRecordArchive,
             deleteArchive: DeleteAccessRecordArchive,
             putArchive: StoreAccessRecordArchive,
             threshold: Int)
            (implicit executionContext: ExecutionContext): Future[Unit] = {
      // 1. get ALL archives
      // 2. turn them into records
      // 3. deduplicate
      // 4. batch them
      // 5. create archives from them
      // 6. insert new archives
      // 7. delete old archives

//      implicit val zdtOrdering = new Ordering[ZonedDateTime] {
//        override def compare(x: ZonedDateTime, y: ZonedDateTime): Int = {
//          if (x < y) return -1
//          if (x == y) return 0
//          return 1
//        }
//      }

      // FIXME going to have to find a more efficient way

      for {
        oldArchives <- getArchive(ZonedDateTime.now().minusYears(39))
        _ = println(s"Got ${oldArchives.size} archives to tidy up")
        records = oldArchives.flatMap(archive => AccessCsv.parse(archive.csv).right.get)
        _ = println(s"Parsed ${records.size} records from the archives")
        deduplicated = records.foldLeft[Seq[Access]](Seq.empty)({
          case (acc, record) if acc.exists(_.id == record.id) => acc
          case (acc, record) => acc :+ record
        })
        _ = println(s"Got ${deduplicated.size} records after de-duplication")
        groups = deduplicated.grouped(threshold)
        _ = println(s"Got ${groups.size} groups of $threshold records")
        newArchives = groups.map(group => (group.head.time -> group.last.time, AccessCsv.render(group))).toSeq
        _ = println(s"Inserting ${newArchives.size} new archives...")
//        _ <- Future.sequence(newArchives.map(x => putArchive(x)))
        _ = println(s"Deleting ${oldArchives.size} old archives...")
//        _ <- Future.sequence(oldArchives.map(deleteArchive))
        _ = println("DONE")
      } yield {

      }
  }

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
