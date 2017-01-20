package duesoldi.markdown

import com.vladsch.flexmark.IParse
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.ast.Node
import duesoldi.markdown.MarkdownDocument.Heading

import scala.collection.JavaConversions._

private object Flexmark {
  import com.vladsch.flexmark.ast

  type Heading = ast.Heading
}

class MarkdownParser {
  def markdown(raw: String): MarkdownDocument = {
    val parser: IParse = Parser.builder().build()
    val document: Node = parser.parse(raw)
    val nodes = document.getChildren.iterator().collect {
      case heading: Flexmark.Heading if heading.getLevel == 1 => Heading(heading.getText.toString, heading.getLevel)
    }
    MarkdownDocument(nodes.toSeq)
  }
}
