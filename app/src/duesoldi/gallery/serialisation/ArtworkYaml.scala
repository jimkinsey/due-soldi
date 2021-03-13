package duesoldi.gallery.serialisation

import duesoldi.gallery.model.{Artwork, Series}
import duesoldi.gallery.serialisation.ArtworkMap.ArtworkMap
import duesoldi.gallery.serialisation.ArtworkYaml.ParseFailure.{Invalid, Malformed}
import hammerspace.markdown.MarkdownDocument
import hammerspace.yaml.Yaml

import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

object ArtworkYaml
{
  import hammerspace.collections.SeqEnhancements._
  import hammerspace.collections.StandardCoercions._

  def parse(yaml: String): Either[ParseFailure, Artwork] =
    for {
      yaml <- Yaml.obj(yaml).left.map(_ => Malformed)
      work <- ArtworkMap.artwork(yaml).left.map(_ => Invalid)
    } yield {
      work
    }

  def parseAll(yaml: String): Either[ParseFailure, Seq[Artwork]] =
    for {
      arr <- Yaml.arr(yaml).left.map(_ => Malformed)
      maps = arr.asSeqOf[ArtworkMap]
      works <- ArtworkMap.artworks(maps).left.map(_ => Invalid)
    } yield {
      works
    }

  def format(work: Artwork): String =
    s"""id: ${work.id}
       |last-modified: ${work.lastModified.format(ISO_ZONED_DATE_TIME)}
       |image-url: ${work.imageURL}
       |timeframe: ${work.timeframe.getOrElse("")}
       |materials: ${work.materials.getOrElse("")}
       |title: ${work.title}
       |series-id: ${work.seriesId.getOrElse("")}
       |description: |
       |${indentBlock(work.description.getOrElse(MarkdownDocument.empty).raw)}""".stripMargin

  def formatAll(works: Seq[Artwork]): String = works
    .map(format)
    .map(work => "- \n" + indentBlock(work))
    .mkString("\n")

  def indentBlock(text: String): String = text.lines.map(indentLine).mkString("\n")
  def indentLine(line: String): String = s"  $line"

  sealed trait ParseFailure
  object ParseFailure
  {
    case object Malformed extends ParseFailure
    case object Invalid extends ParseFailure
  }
}


