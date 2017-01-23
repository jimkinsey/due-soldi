package duesoldi.markdown

import com.vladsch.flexmark.IParse
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.ast.{Block, Node}
import com.vladsch.flexmark.util.sequence.BasedSequence
import duesoldi.markdown.MarkdownDocument._

import scala.collection.JavaConversions._
import scala.language.implicitConversions

private object Flexmark {
  import com.vladsch.flexmark.ast

  type Heading = ast.Heading
  type Paragraph = ast.Paragraph
  type Text = ast.Text
  type Emphasis = ast.Emphasis
  type Strong = ast.StrongEmphasis
  type Code = ast.IndentedCodeBlock
  type Link = ast.Link
}

class MarkdownParser {
  def markdown(raw: String): MarkdownDocument = {
    val parser: IParse = Parser.builder().build()
    val document: Node = parser.parse(raw)
    MarkdownDocument(translated(document.getChildren.toSeq))
  }

  private implicit def basedSequenceToString(basedSequence: BasedSequence): String = basedSequence.toString

  private def translated(nodes: Seq[Node]): Seq[MarkdownDocument.Node] = {
    nodes collect {
      case heading: Flexmark.Heading     => Heading(heading.getText, heading.getLevel)
      case paragraph: Flexmark.Paragraph => Paragraph(translated(paragraph.getChildren.toSeq))
      case text: Flexmark.Text           => Text(text.getChars)
      case strong: Flexmark.Strong       => Strong(strong.getChildChars)
      case emphasis: Flexmark.Emphasis   => Emphasis(emphasis.getChildChars)
      case code: Flexmark.Code           => Code(code.getChars)
      case link: Flexmark.Link           => InlineLink(link.getText, link.getUrl, Option(link.getTitle))
      case node                          => UnsupportedNode(node.getChars, node.getNodeName)
    }
  }
}
