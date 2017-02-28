package duesoldi.storage

import java.sql.{Connection, DriverManager, ResultSet, Timestamp}
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.storage.AccessRecordStore.Access

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait AccessRecordStore {
  def allRecords: Future[Seq[AccessRecordStore.Access]]
  def record(access: Access): Future[Unit]
}

object AccessRecordStore {
  case class Access(time: ZonedDateTime, path: String, referer: Option[String])
}

class JDBCAccessRecordStore(url: String, username: String, password: String)(implicit executionContext: ExecutionContext) extends AccessRecordStore {

  override def allRecords: Future[Seq[Access]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT timestamp, path, referer FROM access_record").map { row =>
        Access(
          path = row.getString(2),
          time = row.getTimestamp(1).toInstant.atZone(ZoneId.of("UTC+1")),
          referer = Option(row.getString(3))
        )
      } toList
    }
  }

  override def record(access: Access): Future[Unit] = Future.fromTry {
    withConnection { connection =>
      val insert = connection.prepareStatement("INSERT INTO access_record ( timestamp, path, referer ) VALUES ( ?, ?, ? )")
      insert.setTimestamp(1, Timestamp.from(access.time.toInstant))
      insert.setString(2, access.path)
      insert.setString(3, access.referer.orNull)
      insert.executeUpdate()
    }
  }

  private def withConnection[T](block: Connection => T): Try[T] = {
    Try(DriverManager.getConnection(url, username, password)).flatMap { connection =>
      val res = Try(block(connection))
      connection.close()
      res
    }
  }

  private def queryResults(query: String)(implicit connection: Connection): Stream[ResultSet] = {
    resultStream(connection.createStatement().executeQuery(query))
  }

  private def resultStream(resultSet: ResultSet): Stream[ResultSet] = {
    resultSet.next() match {
      case false => Stream.empty
      case true  => resultSet #:: resultStream(resultSet)
    }
  }

}

class InMemoryAccessRecordStore extends AccessRecordStore {
  override def allRecords: Future[Seq[Access]] = Future.successful(records)
  override def record(access: Access): Future[Unit] = Future.successful(records.append(access))

  private lazy val records: collection.mutable.ListBuffer[Access] = ListBuffer()
}
