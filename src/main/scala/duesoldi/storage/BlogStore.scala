package duesoldi.storage

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import duesoldi.markdown.{MarkdownDocument, MarkdownParser}
import duesoldi.model.BlogEntry
import duesoldi.storage.BlogStore.Created
import duesoldi.storage.JDBCConnection.ConnectionDetails
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait BlogStore {
  def entry(name: String): Future[Option[BlogEntry]]
  def entries: Future[Seq[BlogEntry]]
  def store(name: String, published: ZonedDateTime, content: String): Future[BlogStore.StoreResult]
  def delete(name: String): Future[Unit]
}

object BlogStore {
  sealed trait StoreResult
  case class Created(blogEntry: BlogEntry) extends StoreResult
  case class Invalid(reasons: Seq[String]) extends StoreResult
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
    validationFailures(name, content) match {
      case Nil     => withConnection { implicit connection =>
        updateResults("INSERT INTO blog_entry ( id, published, content ) VALUES ( ?, ?, ? )", name, Timestamp.from(published.toInstant), content)
        Created(blogEntry(name, published, content))
      }
      case reasons => Success(BlogStore.Invalid(reasons))
    }
  }

  override def delete(name: String): Future[Unit] = Future.fromTry {
    withConnection { implicit connection =>
      updateResults("DELETE FROM blog_entry WHERE id = ?", name)
    }
  }

  private def validationFailures(name: String, content: String): Seq[String] = {
    lazy val markdown = parser.markdown(content)
    lazy val validatedName = ValidIdentifier(name) match {
      case Some(_) => None
      case None    => Some(s"Blog entry identifier '$name' is invalid")
    }
    lazy val contentHasTitle = MarkdownDocument.title(markdown) match {
      case Some(_) => None
      case None    => Some(s"Blog content has no title (level 1 header in the Markdown)")
    }
    Seq(validatedName, contentHasTitle).flatten
  }

  private def blogEntry(id: String, published: ZonedDateTime, content: String): BlogEntry = {
    BlogEntry(
      id = id,
      content = parser.markdown(content),
      lastModified = published
    )
  }

}
