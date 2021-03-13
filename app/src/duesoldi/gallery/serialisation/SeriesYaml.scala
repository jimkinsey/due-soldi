package duesoldi.gallery.serialisation

import duesoldi.gallery.model.Series
import duesoldi.gallery.serialisation.SeriesMap.SeriesMap
import duesoldi.gallery.serialisation.SeriesYaml.ParseFailure.{Invalid, Malformed}
import hammerspace.yaml.Yaml

object SeriesYaml {

  import hammerspace.collections.SeqEnhancements._
  import hammerspace.collections.StandardCoercions._

  def parse(yaml: String): Either[ParseFailure, Series] =
    for {
      yaml <- Yaml.obj(yaml).left.map(_ => Malformed)
      work <- SeriesMap.series(yaml).left.map(_ => Invalid)
    } yield {
      work
    }

  def parseAll(yaml: String): Either[ParseFailure, Seq[Series]] =
    for {
      arr <- Yaml.arr(yaml).left.map(_ => Malformed)
      maps = arr.asSeqOf[SeriesMap]
      works <- SeriesMap.manySeries(maps).left.map(_ => Invalid)
    } yield {
      works
    }

  def format(work: Series): String =
    s"""id: ${work.id}
       |title: ${work.title}
       """.stripMargin
//       |description: |
//       |${indentBlock(work.description.getOrElse(MarkdownDocument.empty).raw)}""".stripMargin

  def formatAll(works: Seq[Series]): String = works
    .map(format)
    .map(work => "- \n" + indentBlock(work))
    .mkString("\n")

  def indentBlock(text: String): String = text.lines.map(indentLine).mkString("\n")

  def indentLine(line: String): String = s"  $line"

  sealed trait ParseFailure

  object ParseFailure {

    case object Malformed extends ParseFailure

    case object Invalid extends ParseFailure

  }

}
