package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument._

object MarkdownToHtmlConverter {

  def html(nodes: Seq[MarkdownDocument.Node]): String = {
    nodes collect {
      case Heading(content, level)      => s"<h$level>${html(content)}</h$level>"
      case Paragraph(content)           => s"<p>${html(content)}</p>"
      case Text(content)                => content
      case Emphasis(content)            => s"<i>$content</i>"
      case Strong(content)              => s"<b>$content</b>"
      case InlineLink(text, url, title) => s"""<a href="$url" title="${title.getOrElse("")}">$text</a>"""
      case Code(content)                => s"<pre><code>$content</code></pre>"
      case InlineCode(content)          => s"<code>$content</code>"
      case UnorderedList(items)         => s"<ul>${items map { itemNodes => s"<li>${html(itemNodes)}</li>"}}</ul>"
      case OrderedList(items)           => s"<ol>${items map { itemNodes => s"<li>${html(itemNodes)}</li>"}}</ol>"
      case LineBreak                    => s"<br/>"
      case BlockQuote(content)          => s"<blockquote>${html(content)}</blockquote>"
      case HorizontalRule               => "<hr/>"
      case Image(alt, src, title)       => s"""<img src="$src" alt="$alt" title="${title.getOrElse("")}" />"""
    } mkString
  }

}
