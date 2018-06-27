package duesoldi.metrics

import java.time.ZonedDateTime

import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.Future

package object storage
{
  type GetAccessRecordLogSize = () => Future[Int]
  type GetAccessRecordsWithCount = (Int) => Future[List[Access]]
  type GetAccessRecords = (ZonedDateTime) => Future[List[Access]]
  type StoreAccessRecord = (Access) => Future[Unit]
  type DeleteAccessRecord = (Access) => Future[DeleteResult]

  type StoreAccessRecordArchive = ((ZonedDateTime, ZonedDateTime), String) => Future[Unit]

  sealed trait DeleteResult
  object DeleteResult
  {
    case class Success(deleted: Int) extends DeleteResult
  }
}
