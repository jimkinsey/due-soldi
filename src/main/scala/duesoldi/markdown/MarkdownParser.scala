package duesoldi.markdown

import com.vladsch.flexmark.{IParse, ast}
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.sequence.BasedSequence
import duesoldi.markdown.MarkdownDocument._

import scala.collection.JavaConversions._
import scala.language.implicitConversions

private object Flexmark {
  type Heading = ast.Heading
  type Paragraph = ast.Paragraph
  type Text = ast.Text
  type Emphasis = ast.Emphasis
  type Strong = ast.StrongEmphasis
  type Code = ast.IndentedCodeBlock
  type InlineCode = ast.Code
  type Link = ast.Link
  type BulletList = ast.BulletList
  type OrderedList = ast.OrderedList
  type BlockQuote = ast.BlockQuote
  type LineBreak = ast.SoftLineBreak
  type HorizontalRule = ast.ThematicBreak
}

class MarkdownParser {
  def markdown(raw: String): MarkdownDocument = {
    val parser: IParse = Parser.builder().build()
    val document: Node = parser.parse(raw)
    MarkdownDocument(translated(document.getChildren.toSeq))
  }

  private implicit def basedSequenceToString(basedSequence: BasedSequence): String = basedSequence.toString

  private def translated(nodes: Seq[ast.Node]): Seq[MarkdownDocument.Node] = {
    nodes map translated
  }

  private def translated(node: ast.Node): MarkdownDocument.Node = {
    node match {
      case heading: Flexmark.Heading     => Heading(heading.getText, heading.getLevel)
      case paragraph: Flexmark.Paragraph => Paragraph(translated(paragraph.getChildren.toSeq))
      case quote: Flexmark.BlockQuote    => BlockQuote(translated(quote.getChildren.toSeq))
      case text: Flexmark.Text           => Text(text.getChars)
      case strong: Flexmark.Strong       => Strong(strong.getChildChars)
      case emphasis: Flexmark.Emphasis   => Emphasis(emphasis.getChildChars)
      case code: Flexmark.Code           => Code(code.getChars)
      case code: Flexmark.InlineCode     => InlineCode(code.getText)
      case link: Flexmark.Link           => InlineLink(link.getText, link.getUrl, Option(link.getTitle))
      case list: Flexmark.BulletList     => UnorderedList(list.getChildren.toSeq.map(c => stripRootPara(translated(c.getChildren.toSeq))))
      case list: Flexmark.OrderedList    => OrderedList(list.getChildren.toSeq.map(c => stripRootPara(translated(c.getChildren.toSeq))))
      case _: Flexmark.LineBreak         => LineBreak
      case _: Flexmark.HorizontalRule    => HorizontalRule
      case _                             => UnsupportedNode(node.getChars, node.getNodeName)
    }
  }

  private def stripRootPara(nodes: Seq[MarkdownDocument.Node]): Seq[MarkdownDocument.Node] = nodes.toList match {
    case Paragraph(content) :: Nil => content
    case _                         => nodes
  }

}
