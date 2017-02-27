package duesoldi.storage

import java.time.ZonedDateTime

import duesoldi.storage.AccessRecordStore.Access

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait AccessRecordStore {
  def allRecords: Future[Seq[AccessRecordStore.Access]]

  def record(access: Access): Future[Unit]
}

object AccessRecordStore {
  case class Access(time: ZonedDateTime, path: String)
}

class InMemoryAccessRecordStore extends AccessRecordStore {
  override def allRecords: Future[Seq[Access]] = Future.successful(accesses)
  override def record(access: Access): Future[Unit] = Future.successful(accesses.append(access))

  private lazy val accesses: collection.mutable.ListBuffer[Access] = ListBuffer()
}
