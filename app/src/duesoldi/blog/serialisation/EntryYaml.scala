package duesoldi.blog.serialisation

import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.serialisation.EntryMap.EntryMap
import duesoldi.blog.serialisation.EntryYaml.ParseFailure.{Invalid, Malformed}
import hammerspace.yaml.Yaml

object EntryYaml
{
  import hammerspace.collections.SeqEnhancements._
  import hammerspace.collections.StandardCoercions._

  def parse(yaml: String): Either[ParseFailure, BlogEntry] =
    for {
      yaml <- Yaml.obj(yaml).left.map(_ => Malformed)
      entry <- EntryMap.entry(yaml).left.map(_ => Invalid)
    } yield {
      entry
    }

  def parseAll(yaml: String): Either[ParseFailure, Seq[BlogEntry]] =
    for {
      arr <- Yaml.arr(yaml).left.map(_ => Malformed)
      maps = arr.asSeqOf[EntryMap]
      entries <- EntryMap.entries(maps).left.map(_ => Invalid)
    } yield {
      entries
    }

  def format(entry: BlogEntry): String =
    s"""id: ${entry.id}
       |description: ${entry.description.getOrElse("")}
       |last-modified: ${entry.lastModified.format(ISO_ZONED_DATE_TIME)}
       |content: |
       |${indentBlock(entry.content.raw)}""".stripMargin

  def formatAll(entries: Seq[BlogEntry]): String = entries
    .map(format)
    .map(entry => "- \n" + indentBlock(entry))
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
