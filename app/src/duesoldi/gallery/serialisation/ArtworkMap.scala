package duesoldi.gallery.serialisation

import java.time.ZonedDateTime
import duesoldi.gallery.model.{Artwork, Series}
import hammerspace.markdown.MarkdownParser

object ArtworkMap
{
  import hammerspace.collections.MapEnhancements._
  import hammerspace.collections.StandardCoercions._

  type ArtworkMap = Map[String,Any]

  sealed trait Failure
  object Failure
  {
    case object MissingTitle extends Failure
    case object MissingId extends Failure
    case object MissingImageURL extends Failure
  }

  def artwork(map: ArtworkMap): Either[Failure, Artwork] =
    for {
      id <- map.field[String]("id").toRight({ Failure.MissingId })
      title <- map.field[String]("title").toRight({ Failure.MissingTitle })
      imageURL <- map.field[String]("image-url").toRight({ Failure.MissingImageURL })
      description = map.field[String]("description").map(MarkdownParser.parseMarkdown)
      timeframe = map.field[String]("timeframe")
      materials = map.field[String]("materials")
      lastModified = map.field[ZonedDateTime]("last-modified").getOrElse(ZonedDateTime.now())
      seriesID = map.field[String]("series-id")
    } yield {
      Artwork(id, title, imageURL, description, lastModified, timeframe, materials, seriesID)
    }

  def artworks(maps: Seq[ArtworkMap]): Either[Failure, Seq[Artwork]] = {
    maps.foldLeft[Either[Failure, Seq[Artwork]]](Right(Seq.empty)) {
      case (Right(acc), map) => artwork(map).map(acc :+ _)
      case (acc, _) => acc
    }
  }
}


