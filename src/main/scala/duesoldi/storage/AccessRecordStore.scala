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
  case class Access(time: ZonedDateTime, path: String, referer: Option[String], userAgent: Option[String], duration: Long)
}

class JDBCAccessRecordStore(val connectionDetails: ConnectionDetails)(implicit executionContext: ExecutionContext) extends AccessRecordStore with JDBCConnection  {

  override def allRecords: Future[Seq[Access]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT timestamp, path, referer, user_agent, duration FROM access_record").map { row =>
        Access(
          path = row.getString(2),
          time = row.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC+1")),
          referer = Option(row.getString(3)),
          userAgent = Option(row.getString(4)),
          duration = row.getString(5).toLong
        )
      } toList
    }
  }

  override def record(access: Access): Future[Unit] = Future.fromTry {
    withConnection { connection =>
      val insert = connection.prepareStatement("INSERT INTO access_record ( timestamp, path, referer, user_agent, duration ) VALUES ( ?, ?, ?, ?, ? )")
      insert.setTimestamp(1, Timestamp.from(access.time.toInstant))
      insert.setString(2, access.path)
      insert.setString(3, access.referer.orNull)
      insert.setString(4, access.userAgent.orNull)
      insert.setLong(5, access.duration)
      insert.executeUpdate()
    }
  }

}

class InMemoryAccessRecordStore extends AccessRecordStore {
  override def allRecords: Future[Seq[Access]] = Future.successful(records)
  override def record(access: Access): Future[Unit] = Future.successful(records.append(access))

  private lazy val records: collection.mutable.ListBuffer[Access] = ListBuffer()
}
