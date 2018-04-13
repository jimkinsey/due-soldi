package duesoldi.metrics.storage

import java.sql.{ResultSet, Timestamp}
import java.time.{ZoneId, ZonedDateTime}

import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.Future

object AccessRecordStore
{
  case class Access(time: ZonedDateTime, path: String, referer: Option[String], userAgent: Option[String], duration: Long, clientIp: Option[String], country: Option[String], statusCode: Int)

  def toAccess: (ResultSet => Access) = { row =>
    Access(
      path = row.getString(2),
      time = row.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC+1")),
      referer = Option(row.getString(3)),
      userAgent = Option(row.getString(4)),
      duration = row.getString(5).toLong,
      clientIp = Option(row.getString(6)),
      country = Option(row.getString(7)),
      statusCode = row.getInt(8)
    )
  }

  def getAll(performQuery: PerformQuery[Access]): GetAccessRecords = (start: ZonedDateTime) => Future.fromTry {
    performQuery("SELECT timestamp, path, referer, user_agent, duration, client_ip, country, status_code FROM access_record WHERE timestamp > ?", Seq(Timestamp.from(start.toInstant)))
  }

  def put(performUpdate: PerformUpdate): StoreAccessRecord = (access) => Future.fromTry {
    performUpdate("INSERT INTO access_record ( timestamp, path, referer, user_agent, duration, client_ip, country, status_code ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )",
      Seq(
        Timestamp.from(access.time.toInstant),
        access.path,
        access.referer.orNull,
        access.userAgent.orNull,
        access.duration,
        access.clientIp.orNull,
        access.country.orNull,
        access.statusCode
      )
    ) map (_ => {})
  }
}
