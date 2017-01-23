package duesoldi.rendering

import duesoldi.markdown.MarkdownDocument
import duesoldi.markdown.MarkdownDocument._
import duesoldi.model.BlogEntry

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq, XML}

class Renderer {
  def render(entry: BlogEntry)(implicit ec: ExecutionContext): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(html(
      <html>
        <head><title>{entry.title}</title></head>
        <body>
          <div id="content">
            <article>
              {html(entry.content.nodes)}
            </article>
          </div>
          <footer>
            <small id="copyright">&copy; 2016-2017 Jim Kinsey</small>
          </footer>
        </body>
      </html>
    ))
  }

  private def html(xml: Elem): String = s"<!DOCTYPE html>\n${xml.mkString}\n"

  private def html(nodes: Seq[MarkdownDocument.Node]): NodeSeq = {
    nodes collect {
      case Heading(text, level)         => XML.loadString(s"<h$level>$text</h$level>")
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

object Renderer {
  sealed trait Failure
}