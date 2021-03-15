package duesoldi.gallery.storage

import duesoldi.gallery.storage.GalleryStore.DeleteResult.Deleted
import duesoldi.gallery.storage.GalleryStore.PutResult.Created
import duesoldi.gallery.model.{Artwork, Series}
import hammerspace.markdown
import hammerspace.storage.JDBCConnection.{PerformQuery, PerformUpdate}

import java.sql.{ResultSet, Timestamp}
import java.time.ZoneId
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object GalleryStore
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

  sealed trait CreateOrUpdateResult
  object CreateOrUpdateResult
  {
    case object Failure extends CreateOrUpdateResult
    sealed trait Success extends CreateOrUpdateResult
    object Success
    {
      case object Updated extends Success
      case object Created extends Success
    }
  }

  def toArtwork(parseMarkdown: markdown.Parse): (ResultSet => Artwork) = { row =>
    Artwork(
      id = row.getString("id"),
      title = row.getString("title"),
      lastModified = row.getTimestamp("last_modified").toInstant.atZone(ZoneId.of("UTC+1")),
      description = Option(parseMarkdown(row.getString("description"))),
      imageURL = row.getString("image_url"),
      timeframe = Option(row.getString("timeframe")),
      materials = Option(row.getString("materials")),
      seriesId = Option(row.getString("series_id"))
    )
  }

  def toSeries(parseMarkdown: markdown.Parse): (ResultSet => Series) = { row =>
    Series(
      id = row.getString("id"),
      title = row.getString("title")
      // TODO description
    )
  }

  def getOne(performQuery: PerformQuery[Artwork])(id: String): Future[Option[Artwork]] = Future.fromTry {
    performQuery("SELECT id, title, last_modified, description, image_url, timeframe, materials, series_id FROM artwork WHERE id = ?", Seq(id)).map {
      _.headOption
    }
  }

  def getOneSeries(performQuery: PerformQuery[Series])(id: String): Future[Option[Series]] = Future.fromTry {
    performQuery("SELECT id, title, description FROM series WHERE id = ?", Seq(id)).map {
      _.headOption
    }
  }

  def getAllSeries(performQuery: PerformQuery[Series]): () => Future[List[Series]] = () => Future.fromTry {
    performQuery("SELECT id, title, description FROM series", Seq.empty)
  }

  def getAll(performQuery: PerformQuery[Artwork]): () => Future[List[Artwork]] = () => Future.fromTry {
    performQuery("SELECT id, title, last_modified, description, image_url, timeframe, materials, series_id FROM artwork", Seq.empty)
  }

  def put(performUpdate: PerformUpdate)(work: Artwork): Future[Either[PutResult.Failure.type,PutResult.Created.type]] = Future.successful {
    performUpdate("INSERT INTO artwork ( id, title, last_modified, description, image_url, timeframe, materials, series_id ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )",
      Seq(
        work.id,
        work.title,
        Timestamp.from(work.lastModified.toInstant),
        work.description.map(_.raw).orNull,
        work.imageURL,
        work.timeframe.orNull,
        work.materials.orNull,
        work.seriesId.orNull
      )
    ) match {
      case Success(_) => Right(PutResult.Created)
      case Failure(f) =>
        Left(PutResult.Failure)
    }
  }

  def putSeries(performUpdate: PerformUpdate)(series: Series): Future[Either[PutResult.Failure.type,PutResult.Created.type]] = Future.successful {
    performUpdate("INSERT INTO series ( id, title, description ) VALUES ( ?, ?, ? )",
      Seq(
        series.id,
        series.title,
        null // TODO
      )
    ) match {
      case Success(_) => Right(PutResult.Created)
      case Failure(f) =>
        System.err.println(s"PUT FAILURE = $f")
        Left(PutResult.Failure)
    }
  }

  def putAll(performUpdate: PerformUpdate)(implicit executionContext: ExecutionContext) = (works: Seq[Artwork]) => {
    Future.sequence(works.map(put(performUpdate))) map (_.collectFirst { case f @ Left(_) => f } getOrElse Right(Created))
  }

  def putAllSeries(performUpdate: PerformUpdate)(implicit executionContext: ExecutionContext) = (series: Seq[Series]) => {
    Future.sequence(series.map(putSeries(performUpdate))) map (_.collectFirst { case f @ Left(_) => f } getOrElse Right(Created))
  }

  def delete(performUpdate: PerformUpdate)(id: String): Future[Either[DeleteResult.Failure.type, DeleteResult.Deleted.type]] = Future.fromTry {
    performUpdate("DELETE FROM artwork WHERE id = ?", Seq(id)) map (_ => { Right(Deleted) }) recover { case _ => Left(DeleteResult.Failure) }
  }

  def deleteAll(performUpdate: PerformUpdate) = () => Future.fromTry {
    performUpdate("DELETE FROM artwork", Seq.empty) map (_ => {
      Right(Deleted)
    }) recover {
      case _ => Left(DeleteResult.Failure)
    }
  }

  def createOrUpdate(get: GetArtwork, delete: DeleteArtwork, put: PutArtwork)(implicit executionContext: ExecutionContext): CreateOrUpdateArtwork = entry => {
    get(entry.id) flatMap {
      case Some(_) =>
        for {
          _ <- delete(entry.id).map(_.left.map(_ => CreateOrUpdateResult.Failure))
          _ <- put(entry).map(_.left.map(_ => CreateOrUpdateResult.Failure))
        } yield {
          Right(CreateOrUpdateResult.Success.Updated)
        }
      case None =>
        put(entry) map { _ => Right(CreateOrUpdateResult.Success.Created) }
    }
  }
}
