package duesoldi.storage

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.markdown.MarkdownParser
import duesoldi.model.BlogEntry
import duesoldi.storage.JDBCConnection.ConnectionDetails

import scala.concurrent.{ExecutionContext, Future}

trait BlogStore {
  def entry(name: String): Future[Option[BlogEntry]]
  def entries: Future[Seq[BlogEntry]]
  def store(name: String, published: ZonedDateTime, content: String): Future[BlogEntry]
  def delete(name: String): Future[Unit]
}

class JDBCBlogStore(val connectionDetails: ConnectionDetails, parser: MarkdownParser)(implicit executionContext: ExecutionContext) extends BlogStore with JDBCConnection {

  override def entry(name: String): Future[Option[BlogEntry]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT id, published, content FROM blog_entry WHERE id = ?", name).map { row =>
        blogEntry(
          id = row.getString(1),
          content = row.getString(3),
          published = row.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC+1"))
        )
      }.toList.headOption
    }
  }

  override def entries: Future[Seq[BlogEntry]] = Future.fromTry {
    withConnection { implicit connection =>
      queryResults("SELECT id, published, content FROM blog_entry").map { row =>
        blogEntry(
          id = row.getString(1),
          content = row.getString(3),
          published = row.getTimestamp(2).toInstant.atZone(ZoneId.of("UTC+1"))
        )
      }.toList
    }
  }

  override def store(name: String, published: ZonedDateTime, content: String) = Future.fromTry {
    withConnection { implicit connection =>
      updateResults("INSERT INTO blog_entry ( id, published, content ) VALUES ( ?, ?, ? )", name, Timestamp.from(published.toInstant), content)
      blogEntry(name, published, content)
    }
  }

  override def delete(name: String): Future[Unit] = Future.fromTry {
    withConnection { implicit connection =>
      updateResults("DELETE FROM blog_entry WHERE id = ?", name)
    }
  }

  private def blogEntry(id: String, published: ZonedDateTime, content: String): BlogEntry = {
    BlogEntry(
      id = id,
      content = parser.markdown(content),
      lastModified = published
    )
  }

}
