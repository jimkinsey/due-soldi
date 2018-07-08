package duesoldi.metrics.storage

import java.sql.{ResultSet, Timestamp}
import java.time.{ZoneId, ZonedDateTime}

import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.Future

object AccessRecordArchiveStore
{
  type Archive = ((ZonedDateTime, ZonedDateTime), String)

  val toArchive: ResultSet => Archive = (resultSet) => {
    val from = resultSet.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC"))
    val to = resultSet.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC"))
    val csv = resultSet.getString(3)
    (from -> to, csv)
  }

  def get(performQuery: PerformQuery[Archive]): GetAccessRecordArchive = start => Future.fromTry {
    performQuery("SELECT * FROM access_record_archive WHERE toTime > ?", Seq(Timestamp.from(start.toInstant)))
  }

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
