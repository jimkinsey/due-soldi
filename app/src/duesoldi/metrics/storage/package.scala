package duesoldi.metrics

import java.time.ZonedDateTime

import duesoldi.metrics.rendering.AccessCsv
import duesoldi.metrics.storage.AccessRecordArchiveStore.Archive
import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.Future

package object storage
{
  type GetAccessRecordLogSize = () => Future[Int]
  type GetAccessRecordsWithCount = (Int) => Future[List[Access]]
  type GetAccessRecords = (ZonedDateTime) => Future[List[Access]]
  type StoreAccessRecord = (Access) => Future[Unit]
  type DeleteAccessRecord = (Access) => Future[DeleteResult]
  type UpdateAccessRecord = (Access) => Future[UpdateResult]

  type StoreAccessRecordArchive = ((ZonedDateTime, ZonedDateTime), String) => Future[Unit]
  type GetAccessRecordArchive = ZonedDateTime => Future[List[Archive]]
  type DeleteAccessRecordArchive = Archive => Future[ArchiveDeleteResult]

  type GetAllAccessRecords = ZonedDateTime => Future[Either[AccessCsv.ParseFailure, List[Access]]]

  sealed trait DeleteResult
  object DeleteResult
  {
    case class Success(deleted: Int) extends DeleteResult
  }

  sealed trait UpdateResult
  object UpdateResult
  {
    case class Success(updated: Int) extends UpdateResult
  }

  sealed trait ArchiveDeleteResult
  object ArchiveDeleteResult
  {
    case class Success(deleted: Int) extends ArchiveDeleteResult
  }
}
