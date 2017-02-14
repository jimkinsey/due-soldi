package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument._

import scala.xml.NodeSeq

/**
  * Created by jimkinsey on 14/02/17.
  */
object MarkdownToHtmlConverter {

  def html(nodes: Seq[MarkdownDocument.Node]): NodeSeq = {
    nodes collect {
      case Heading(content, level)      => <_>{html(content)}</_>.copy(label = s"h$level")
      case Paragraph(content)           => <p>{html(content)}</p>
      case Text(content)                => <_>{content}</_>.child.head
      case Emphasis(content)            => <i>{content}</i>
      case Strong(content)              => <b>{content}</b>
      case InlineLink(text, url, title) => <a href={url} title={title.orNull}>{text}</a>
      case Code(content)                => <pre><code>{content}</code></pre>
      case InlineCode(content)          => <code>{content}</code>
      case UnorderedList(items)         => <ul>{items map { itemNodes => <li>{html(itemNodes)}</li>}}</ul>
      case OrderedList(items)           => <ol>{items map { itemNodes => <li>{html(itemNodes)}</li>}}</ol>
      case LineBreak                    => <br/>
      case BlockQuote(content)          => <blockquote>{html(content)}</blockquote>
      case HorizontalRule               => <hr/>
    }
  }

}
