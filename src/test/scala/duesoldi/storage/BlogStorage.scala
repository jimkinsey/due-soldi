package duesoldi.storage

import java.sql.{DriverManager, Timestamp}
import java.time.ZonedDateTime

import duesoldi.{Env, Setup}

import scala.concurrent.{ExecutionContext, Future}

trait BlogStorage {

  case class EntryBuilder(id: String = "id", content: String = "# Title", lastModified: ZonedDateTime = ZonedDateTime.now())

  implicit def tupleToBuilder(tuple: (String, String)): EntryBuilder = tuple match {
    case (id, content) => EntryBuilder(id, content)
  }

  implicit def tuple3ToBuilder(tuple: (String, String, String)): EntryBuilder = tuple match {
    case (time, id, content) => EntryBuilder(id, content, ZonedDateTime.parse(time))
  }

  def blogEntries(entries: EntryBuilder*)(implicit executionContext: ExecutionContext) = new Setup {
    override def setup(env: Env): Future[Env] = {
      val url = env("JDBC_DATABASE_URL")
      val username = env("JDBC_DATABASE_USERNAME")
      val password = env("JDBC_DATABASE_PASSWORD")
      Future {
        val connection = DriverManager.getConnection(url, username, password)
        try {
          entries.foreach { entry =>
            val insert = connection.prepareStatement("INSERT INTO blog_entry (id, published, content) VALUES ( ?, ?, ? )")
            insert.setString(1, entry.id)
            insert.setTimestamp(2, Timestamp.from(entry.lastModified.toInstant))
            insert.setString(3, entry.content)
            insert.executeUpdate()
          }
        }
        finally {
          connection.close()
        }
        Map.empty
      }
    }
  }

}

object BlogStorage extends BlogStorage