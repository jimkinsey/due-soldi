package duesoldi.metrics

import duesoldi.metrics.storage.AccessRecordStore.Access

import scala.concurrent.Future

package object storage
{
  type GetAllAccessRecords = () => Future[List[Access]]
  type StoreAccessRecord = (Access) => Future[Unit]
}
