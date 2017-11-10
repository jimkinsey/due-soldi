package duesoldi.blog.storage

import java.sql.{ResultSet, Timestamp}
import java.time.ZoneId

import duesoldi.blog.model.BlogEntry
import duesoldi.markdown
import duesoldi.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object BlogStore
{
  sealed trait PutResult
  object PutResult {
    case object Created extends PutResult
    case object Failure extends PutResult
  }

  def toBlogEntry(parseMarkdown: markdown.Parse): (ResultSet => BlogEntry) = { row =>
    BlogEntry(
      id = row.getString("id"),
      lastModified = row.getTimestamp("published").toInstant.atZone(ZoneId.of("UTC+1")),
      content = parseMarkdown(row.getString("content"))
    )
  }

  def getOne(performQuery: PerformQuery[BlogEntry])(name: String): Future[Option[BlogEntry]] = Future.fromTry {
    performQuery("SELECT id, published, content FROM blog_entry WHERE id = ?", Seq(name)).map {
      _.headOption
    }
  }

  def getAll(performQuery: PerformQuery[BlogEntry]): () => Future[List[BlogEntry]] = () => Future.fromTry {
    performQuery("SELECT id, published, content FROM blog_entry", Seq.empty)
  }

  def put(performUpdate: PerformUpdate)(entry: BlogEntry): Future[Either[PutResult.Failure.type,PutResult.Created.type]] = Future.successful {
    performUpdate("INSERT INTO blog_entry ( id, published, content ) VALUES ( ?, ?, ? )",
      Seq(
        entry.id,
        Timestamp.from(entry.lastModified.toInstant),
        entry.content.raw
      )
    ) match {
      case Success(_) => Right(PutResult.Created)
      case Failure(_) => Left(PutResult.Failure)
    }
  }

  def delete(performUpdate: PerformUpdate)(name: String): Future[Unit] = Future.fromTry {
    performUpdate("DELETE FROM blog_entry WHERE id = ?", Seq(name)) map (_ => {})
  }
}
