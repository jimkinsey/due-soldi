package duesoldi.storage

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import scala.concurrent.{ExecutionContext, Future}

case class MarkdownContainer(lastModified: ZonedDateTime = ZonedDateTime.now(), content: String)

trait MarkdownSource {
  def document(id: String): Future[Option[MarkdownContainer]]
  def documents: Future[Seq[(String, MarkdownContainer)]]
  def store(id: String, markdown: MarkdownContainer): Future[Unit]
}

class JDBCMarkdownSource(val url: String, val username: String, val password: String)(implicit executionContext: ExecutionContext) extends MarkdownSource with JDBCConnection {
  override def document(id: String): Future[Option[MarkdownContainer]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT id, published, content FROM blog_entry").map { row =>
        MarkdownContainer(
          lastModified = row.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC+1")),
          content = row.getString(3)
        )
      }.toList.headOption
    }
  }

  override def documents: Future[Seq[(String, MarkdownContainer)]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT id, published, content FROM blog_entry").map { row =>
        row.getString(1) -> MarkdownContainer(
          lastModified = row.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC+1")),
          content = row.getString(3)
        )
      }.toList
    }
  }

  override def store(id: String, markdown: MarkdownContainer) = Future.fromTry {
    withConnection { implicit connection =>
      val insert = connection.prepareStatement("INSERT INTO blog_entry ( id, published, content ) VALUES ( ?, ?, ?)")
      insert.setString(1, id)
      insert.setTimestamp(2, Timestamp.from(markdown.lastModified.toInstant))
      insert.setString(3, markdown.content)
      insert.executeUpdate()
    }
  }
}
