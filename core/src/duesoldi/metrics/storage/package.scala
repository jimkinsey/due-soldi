package duesoldi.metrics

import java.time.ZonedDateTime

import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.Future

package object storage
{
  type GetAccessRecords = (ZonedDateTime) => Future[List[Access]]
  type StoreAccessRecord = (Access) => Future[Unit]
}
