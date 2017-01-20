package duesoldi.markdown

object MarkdownDocument {
  sealed trait Node
  case class Heading(content: String, level: Int) extends Node
}

case class MarkdownDocument(nodes: Seq[MarkdownDocument.Node])
