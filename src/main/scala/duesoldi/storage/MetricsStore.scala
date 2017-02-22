package duesoldi.storage

import java.time.ZonedDateTime

import duesoldi.storage.MetricsStore.Access

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait MetricsStore {
  def allMetrics: Future[Seq[MetricsStore.Access]]

  def record(access: Access): Future[Unit]
}

object MetricsStore {
  case class Access(time: ZonedDateTime, path: String)
}

class InMemoryMetricsStore extends MetricsStore {
  override def allMetrics: Future[Seq[Access]] = Future.successful(accesses)
  override def record(access: Access): Future[Unit] = Future.successful(accesses.append(access))

  private lazy val accesses: collection.mutable.ListBuffer[Access] = ListBuffer()
}
