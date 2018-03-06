package duesoldi.markdown

object MarkdownDocument {
  sealed trait Node
  sealed trait Container extends Node {
    def items: Seq[Node]
  }
  sealed trait TextNode extends Node {
    def content: String
  }
  case class Heading(items: Seq[Node], level: Int) extends Container
  case class Paragraph(items: Seq[Node]) extends Container
  case class Text(content: String) extends TextNode
  case class Emphasis(content: String) extends TextNode
  case class Strong(content: String) extends TextNode
  case class Code(content: String) extends TextNode
  case class InlineCode(content: String) extends TextNode
  case class UnsupportedNode(content: String, nodeType: String) extends Node
  case class InlineLink(content: String, link: String, title: Option[String]) extends TextNode
  case class UnorderedList(items: Seq[Seq[Node]]) extends Node
  case class OrderedList(items: Seq[Seq[Node]]) extends Node
  case class BlockQuote(items: Seq[Node]) extends Container
  case object LineBreak extends Node
  case object HorizontalRule extends Node
  case class Image(alt: String, src: String, title: Option[String]) extends Node
  case class HtmlBlock(html: String) extends Node

  def text(nodes: Seq[Node]): String = nodes.foldLeft("") {
    case (acc, container: Container) => acc + text(container.items)
    case (acc, text: TextNode)       => acc + text.content
    case (acc, _)                    => acc
  }

  def title(markdown: MarkdownDocument): Option[String] = markdown.nodes.collectFirst {
    case Heading(nodes, 1) => MarkdownDocument.text(nodes)
  }

  def collectFirst[T](nodes: Seq[Node])(pf: PartialFunction[Node,T]): Option[T] = nodes.foldLeft[Option[T]](None) {
    case (None, node) if pf.isDefinedAt(node) => Some(pf(node))
    case (None, node: Container) => collectFirst[T](node.items)(pf)
    case (acc, _) => acc
  }

  def content(markdown: MarkdownDocument): Seq[Node] = markdown.nodes.dropWhile {
    case Heading(_, 1) => true
    case _ => false
  }

  lazy val empty = MarkdownDocument(Seq.empty, "")
}

case class MarkdownDocument(nodes: Seq[MarkdownDocument.Node], raw: String)
