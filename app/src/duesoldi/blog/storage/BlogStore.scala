package duesoldi.blog.storage

import java.sql.{ResultSet, Timestamp}
import java.time.ZoneId

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.storage.BlogStore.DeleteResult.Deleted
import duesoldi.blog.storage.BlogStore.PutResult.Created
import duesoldi.markdown
import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object BlogStore
{
  sealed trait PutResult
  object PutResult
  {
    case object Created extends PutResult
    case object Failure extends PutResult
  }

  sealed trait DeleteResult
  object DeleteResult
  {
    case object Deleted extends DeleteResult
    case object Failure extends DeleteResult
  }

  def toBlogEntry(parseMarkdown: markdown.Parse): (ResultSet => BlogEntry) = { row =>
    BlogEntry(
      id = row.getString("id"),
      description = Option(row.getString("description")),
      lastModified = row.getTimestamp("published").toInstant.atZone(ZoneId.of("UTC+1")),
      content = parseMarkdown(row.getString("content"))
    )
  }

  def getOne(performQuery: PerformQuery[BlogEntry])(name: String): Future[Option[BlogEntry]] = Future.fromTry {
    performQuery("SELECT id, description, published, content FROM blog_entry WHERE id = ?", Seq(name)).map {
      _.headOption
    }
  }

  def getAll(performQuery: PerformQuery[BlogEntry]): () => Future[List[BlogEntry]] = () => Future.fromTry {
    performQuery("SELECT id, description, published, content FROM blog_entry", Seq.empty)
  }

  def put(performUpdate: PerformUpdate)(entry: BlogEntry): Future[Either[PutResult.Failure.type,PutResult.Created.type]] = Future.successful {
    performUpdate("INSERT INTO blog_entry ( id, description, published, content ) VALUES ( ?, ?, ?, ? )",
      Seq(
        entry.id,
        entry.description.orNull,
        Timestamp.from(entry.lastModified.toInstant),
        entry.content.raw
      )
    ) match {
      case Success(_) => Right(PutResult.Created)
      case Failure(_) => Left(PutResult.Failure)
    }
  }

  def putAll(performUpdate: PerformUpdate)(implicit executionContext: ExecutionContext): PutBlogEntries = (entries) => {
    Future.sequence(entries.map(put(performUpdate))) map (_.collectFirst { case f @ Left(_) => f } getOrElse Right(Created))
  }

  def delete(performUpdate: PerformUpdate)(name: String): Future[Either[DeleteResult.Failure.type, DeleteResult.Deleted.type]] = Future.fromTry {
    performUpdate("DELETE FROM blog_entry WHERE id = ?", Seq(name)) map (_ => { Right(Deleted) }) recover { case _ => Left(DeleteResult.Failure) }
  }

  def deleteAll(performUpdate: PerformUpdate): DeleteAllBlogEntries = () => Future.fromTry {
    performUpdate("DELETE FROM blog_entry", Seq.empty) map (_ => {
      Right(Deleted)
    }) recover {
      case _ => Left(DeleteResult.Failure)
    }
  }
}
