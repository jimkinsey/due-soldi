package duesoldi.metrics.storage

import java.sql.{ResultSet, Timestamp}
import java.time.{ZoneId, ZonedDateTime}

import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.Future

object AccessRecordStore
{
  case class Access(time: ZonedDateTime, path: String, referer: Option[String], userAgent: Option[String], duration: Long, clientIp: Option[String], country: Option[String], statusCode: Int, id: String)

  def toAccess: (ResultSet => Access) = { row =>
    Access(
      path = row.getString(2),
      time = row.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC+1")), // FIXME
      referer = Option(row.getString(3)),
      userAgent = Option(row.getString(4)),
      duration = row.getString(5).toLong,
      clientIp = Option(row.getString(6)),
      country = Option(row.getString(7)),
      statusCode = row.getInt(8),
      id = row.getString(9)
    )
  }

  def getAll(performQuery: PerformQuery[Access]): GetAccessRecords = (start: ZonedDateTime) => Future.fromTry {
    performQuery("SELECT timestamp, path, referer, user_agent, duration, client_ip, country, status_code, request_id FROM access_record WHERE timestamp > ?", Seq(Timestamp.from(start.toInstant)))
  }

  def getAllWithCount(performQuery: PerformQuery[Access]): GetAccessRecordsWithCount = (count: Int) => Future.fromTry {
    performQuery("SELECT timestamp, path, referer, user_agent, duration, client_ip, country, status_code, request_id FROM access_record ORDER BY timestamp LIMIT ?", Seq(count))
  }

  def getLogSize(performQuery: PerformQuery[Access]): GetAccessRecordLogSize = () => Future.fromTry {
    // FIXME should be count(*)
    performQuery("SELECT timestamp, path, referer, user_agent, duration, client_ip, country, status_code, request_id FROM access_record", Seq.empty).map(_.size)
  }

  def put(performUpdate: PerformUpdate): StoreAccessRecord = (access) => Future.fromTry {
    performUpdate("INSERT INTO access_record ( timestamp, path, referer, user_agent, duration, client_ip, country, status_code, request_id ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )",
      Seq(
        Timestamp.from(access.time.toInstant),
        access.path,
        access.referer.orNull,
        access.userAgent.orNull,
        access.duration,
        access.clientIp.orNull,
        access.country.orNull,
        access.statusCode,
        access.id
      )
    ) map (_ => {})
  }

  def update(performUpdate: PerformUpdate): UpdateAccessRecord = (access) => Future.fromTry {
    performUpdate("UPDATE access_record SET ( timestamp, path, referer, user_agent, duration, client_ip, country, status_code ) = ( ?, ?, ?, ?, ?, ?, ?, ? ) WHERE request_id = ?",
      Seq(
        Timestamp.from(access.time.toInstant),
        access.path,
        access.referer.orNull,
        access.userAgent.orNull,
        access.duration,
        access.clientIp.orNull,
        access.country.orNull,
        access.statusCode,
        access.id
      )
    ) map (count => UpdateResult.Success(count))
  }

  def delete(performUpdate: PerformUpdate): DeleteAccessRecord = (access) => Future.fromTry {
    performUpdate("DELETE FROM access_record WHERE request_id = ?",
      Seq(access.id)
    ) map DeleteResult.Success
  }

  def deleteAll(performUpdate: PerformUpdate): DeleteAccessRecords = (records) => Future.fromTry {
    performUpdate("DELETE FROM access_record WHERE request_id = ANY (?)",
      s"{${records.map(_.id).mkString(",")}}"
    ) map DeleteResult.Success
  }
}

