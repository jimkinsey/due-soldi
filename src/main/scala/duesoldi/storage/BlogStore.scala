package duesoldi.storage

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.markdown.{MarkdownDocument, MarkdownParser}
import duesoldi.model.BlogEntry
import duesoldi.storage.BlogStore.{Created, Failure}
import duesoldi.storage.JDBCConnection.ConnectionDetails
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

trait BlogStore {
  def entry(name: String): Future[Option[BlogEntry]]
  def entries: Future[Seq[BlogEntry]]
  def store(entry: BlogEntry): Future[Either[Failure.type, Created.type]]
  def delete(name: String): Future[Unit]
}

object BlogStore {
  sealed trait StoreResult
  case object Created extends StoreResult
  case object Failure extends StoreResult
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

  override def store(entry: BlogEntry): Future[Either[Failure.type , Created.type]] = Future.fromTry {
    withConnection { implicit connection =>
      updateResults("INSERT INTO blog_entry ( id, published, content ) VALUES ( ?, ?, ? )", entry.id, Timestamp.from(entry.lastModified.toInstant), entry.content.raw)
      Right(Created)
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
