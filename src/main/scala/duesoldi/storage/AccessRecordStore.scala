package duesoldi.storage

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.storage.AccessRecordStore.Access
import duesoldi.storage.JDBCConnection.ConnectionDetails

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

trait AccessRecordStore {
  def allRecords: Future[Seq[AccessRecordStore.Access]]
  def record(access: Access): Future[Unit]
}

object AccessRecordStore {
  case class Access(time: ZonedDateTime, path: String, referer: Option[String], userAgent: Option[String], duration: Long, clientIp: Option[String])
}

class JDBCAccessRecordStore(val connectionDetails: ConnectionDetails)(implicit executionContext: ExecutionContext) extends AccessRecordStore with JDBCConnection  {

  override def allRecords: Future[Seq[Access]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT timestamp, path, referer, user_agent, duration, client_ip FROM access_record").map { row =>
        Access(
          path = row.getString(2),
          time = row.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC+1")),
          referer = Option(row.getString(3)),
          userAgent = Option(row.getString(4)),
          duration = row.getString(5).toLong,
          clientIp = Option(row.getString(6))
        )
      } toList
    }
  }

  override def record(access: Access): Future[Unit] = Future.fromTry {
    withConnection { implicit connection =>
      updateResults("INSERT INTO access_record ( timestamp, path, referer, user_agent, duration, client_ip ) VALUES ( ?, ?, ?, ?, ?, ? )",
        Timestamp.from(access.time.toInstant),
        access.path,
        access.referer.orNull,
        access.userAgent.orNull,
        access.duration,
        access.clientIp.orNull
      )
    }
  }

}

class InMemoryAccessRecordStore extends AccessRecordStore {
  override def allRecords: Future[Seq[Access]] = Future.successful(records)
  override def record(access: Access): Future[Unit] = Future.successful(records.append(access))

  private lazy val records: collection.mutable.ListBuffer[Access] = ListBuffer()
}
