package duesoldi.markdown

import com.vladsch.flexmark.IParse
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.ast.{Block, Node}
import duesoldi.markdown.MarkdownDocument._

import scala.collection.JavaConversions._

private object Flexmark {
  import com.vladsch.flexmark.ast

  type Heading = ast.Heading
  type Paragraph = ast.Paragraph
  type Text = ast.Text
  type Emphasis = ast.Emphasis
  type Strong = ast.StrongEmphasis
  type Code = ast.IndentedCodeBlock
}

class MarkdownParser {
  def markdown(raw: String): MarkdownDocument = {
    val parser: IParse = Parser.builder().build()
    val document: Node = parser.parse(raw)
    MarkdownDocument(translated(document.getChildren.toSeq))
  }

  private def translated(nodes: Seq[Node]): Seq[MarkdownDocument.Node] = {
    nodes collect {
      case heading: Flexmark.Heading     => Heading(heading.getText.toString, heading.getLevel)
      case paragraph: Flexmark.Paragraph => Paragraph(translated(paragraph.getChildren.toSeq))
      case text: Flexmark.Text           => Text(text.getChars.toString)
      case strong: Flexmark.Strong       => Strong(strong.getChildChars.toString)
      case emphasis: Flexmark.Emphasis   => Emphasis(emphasis.getChildChars.toString)
      case code: Flexmark.Code           => Code(code.getChars.toString)
      case node                          => UnsupportedNode(node.getChars.toString, node.getNodeName)
    }
  }
}
