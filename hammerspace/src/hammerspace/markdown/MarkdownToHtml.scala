package hammerspace.markdown

import hammerspace.markdown.MarkdownDocument._
import scala.language.postfixOps

object MarkdownToHtml
{
  def html(nodes: Seq[MarkdownDocument.Node]): String = {
    nodes collect {
      case Heading(content, level)      => s"<h$level>${html(content)}</h$level>"
      case Paragraph(content)           => s"<p>${html(content)}</p>"
      case Text(content)                => content
      case Emphasis(content)            => s"<i>$content</i>"
      case Strong(content)              => s"<b>$content</b>"
      case InlineLink(text, url, title) => s"""<a href="$url" title="${title.getOrElse("")}">$text</a>"""
      case Code(content)                => s"""<code class="block">$content</code>"""
      case InlineCode(content)          => s"<code>$content</code>"
      case UnorderedList(items)         => s"<ul>${items map { itemNodes => s"<li>${html(itemNodes)}</li>"} mkString}</ul>"
      case OrderedList(items)           => s"<ol>${items map { itemNodes => s"<li>${html(itemNodes)}</li>"} mkString}</ol>"
      case LineBreak                    => s"<br/>"
      case BlockQuote(content)          => s"<blockquote>${html(content)}</blockquote>"
      case HorizontalRule               => "<hr/>"
      case Image(alt, src, title)       => s"""<img src="$src" alt="$alt" title="${title.getOrElse("")}" />"""
      case HtmlBlock(html)              => html
    } mkString
  }
}
