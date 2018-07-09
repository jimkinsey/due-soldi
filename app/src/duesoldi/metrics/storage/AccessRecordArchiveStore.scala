package duesoldi.metrics.storage

import java.sql.{ResultSet, Timestamp}
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.Future

object AccessRecordArchiveStore
{
  case class Archive(id: String, range: (ZonedDateTime, ZonedDateTime), csv: String)

  val toArchive: ResultSet => Archive = resultSet => {
    val id = resultSet.getString(1)
    val from = resultSet.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC"))
    val to = resultSet.getTimestamp(3).toInstant.atZone(ZoneId.of("UTC"))
    val csv = resultSet.getString(4)
    Archive(id, from -> to, csv)
  }

  def get(performQuery: PerformQuery[Archive]): GetAccessRecordArchive = start => Future.fromTry {
    performQuery("SELECT id, fromTime, toTime, csv FROM access_record_archive WHERE toTime > ?", Seq(Timestamp.from(start.toInstant)))
  }

  def put(performUpdate: PerformUpdate): StoreAccessRecordArchive = (range, csv) => Future.fromTry {
    val (from, to) = range
    performUpdate("INSERT INTO access_record_archive ( id, fromTime, toTime, csv ) VALUES ( ?, ?, ?, ? )",
      Seq(
        UUID.randomUUID().toString,
        Timestamp.from(from.toInstant),
        Timestamp.from(to.toInstant),
        csv
      )
    ) map (_ => {})
  }
}
