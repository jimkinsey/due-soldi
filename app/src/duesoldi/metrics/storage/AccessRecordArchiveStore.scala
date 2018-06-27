package duesoldi.metrics.storage

import java.sql.Timestamp

import hammerspace.storage.JDBCConnection.PerformUpdate

import scala.concurrent.Future

object AccessRecordArchiveStore
{
  def put(performUpdate: PerformUpdate): StoreAccessRecordArchive = (range, csv) => Future.fromTry {
    val (from, to) = range
    performUpdate("INSERT INTO access_record_archive ( fromTime, toTime, csv ) VALUES ( ?, ?, ? )",
      Seq(
        Timestamp.from(from.toInstant),
        Timestamp.from(to.toInstant),
        csv
      )
    ) map (_ => {})
  }
}
