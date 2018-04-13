package duesoldi.blog.serialisation

import java.time.ZonedDateTime

import duesoldi.blog.model.BlogEntry
import hammerspace.markdown.MarkdownParser

object EntryMap
{
  import hammerspace.collections.MapEnhancements._
  import hammerspace.collections.StandardCoercions._

  type EntryMap = Map[String,Any]

  sealed trait Failure
  object Failure
  {
    case object MissingContent extends Failure
    case object MissingId extends Failure
  }

  def entry(map: EntryMap): Either[Failure, BlogEntry] =
    for {
      id <- map.field[String]("id").toRight({ Failure.MissingId })
      content <- map.field[String]("content").toRight({ Failure.MissingContent })
      description = map.field[String]("description")
      markdown = MarkdownParser.parseMarkdown(content)
      lastModified = map.field[ZonedDateTime]("last-modified").getOrElse(ZonedDateTime.now())
    } yield {
      BlogEntry(id, markdown, lastModified, description)
    }

  def entries(maps: Seq[EntryMap]): Either[Failure, Seq[BlogEntry]] = {
    maps.foldLeft[Either[Failure, Seq[BlogEntry]]](Right(Seq.empty)) {
      case (Right(acc), map) => entry(map).map(acc :+ _)
      case (acc, _) => acc
    }
  }
}
