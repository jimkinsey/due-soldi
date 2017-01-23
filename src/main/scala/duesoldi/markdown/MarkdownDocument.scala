package duesoldi.markdown

object MarkdownDocument {
  sealed trait Node
  case class Heading(content: String, level: Int) extends Node
  case class Paragraph(content: Seq[Node]) extends Node
  case class Text(content: String) extends Node
  case class Emphasis(content: String) extends Node
  case class Strong(content: String) extends Node
  case class Code(content: String) extends Node
  case class InlineCode(content: String) extends Node
  case class UnsupportedNode(content: String, nodeType: String) extends Node
  case class InlineLink(text: String, link: String, title: Option[String]) extends Node
  case class UnorderedList(items: Seq[Seq[Node]]) extends Node
  case class OrderedList(items: Seq[Seq[Node]]) extends Node
  case class BlockQuote(content: Seq[Node]) extends Node
  case object LineBreak extends Node
  case object HorizontalRule extends Node
}

case class MarkdownDocument(nodes: Seq[MarkdownDocument.Node])
