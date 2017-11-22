package duesoldi.blog.serialisation

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.serialisation.EntryYaml.ParseFailure.{Malformed, MissingContent, MissingId}
import duesoldi.markdown.MarkdownParser
import duesoldi.yaml.YamlObject

object EntryYaml
{
  import duesoldi.collections.MapEnhancements._

  def parse(yaml: String): Either[EntryYaml.ParseFailure, BlogEntry] =
    for {
      yaml <- YamlObject.parse(yaml).left.map(_ => Malformed)
      id <- yaml.field[String]("id").toRight({ MissingId })
      content <- yaml.field[String]("content").toRight({ MissingContent })
      description = yaml.field[String]("description")
      markdown = MarkdownParser.parseMarkdown(content)
      lastModified = yaml.field[ZonedDateTime]("last-modified")
    } yield {
      BlogEntry(id, markdown, description = description, lastModified = lastModified.getOrElse(ZonedDateTime.now()))
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
    case object MissingContent extends ParseFailure
    case object MissingId extends ParseFailure
    case object Malformed extends ParseFailure
  }
}
