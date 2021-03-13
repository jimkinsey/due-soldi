package duesoldi.gallery.serialisation

import duesoldi.gallery.model.Series

object SeriesMap {

  import hammerspace.collections.MapEnhancements._
  import hammerspace.collections.StandardCoercions._

  type SeriesMap = Map[String, Any]

  sealed trait Failure

  object Failure {
    case object MissingTitle extends Failure
    case object MissingId extends Failure
    case object MissingImageURL extends Failure
  }

  def series(map: SeriesMap): Either[Failure, Series] =
    for {
      id <- map.field[String]("id") toRight { Failure.MissingId }
      title <- map.field[String]("title") toRight { Failure.MissingTitle }
      //      TODO description = map.field[String]("description").map(MarkdownParser.parseMarkdown)
    } yield {
      Series(id, title)
    }

  def manySeries(maps: Seq[SeriesMap]): Either[Failure, Seq[Series]] = {
    maps.foldLeft[Either[Failure, Seq[Series]]](Right(Seq.empty)) {
      case (Right(acc), map) => series(map).map(acc :+ _)
      case (acc, _) => acc
    }
  }
}
