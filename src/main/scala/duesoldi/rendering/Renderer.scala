package duesoldi.rendering

import java.time.format.DateTimeFormatter

import duesoldi.markdown.MarkdownDocument
import duesoldi.markdown.MarkdownDocument._
import duesoldi.model.BlogEntry

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

class Renderer(implicit ec: ExecutionContext) {

  def render(entries: Seq[BlogEntry]): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(html(
      <html>
        <head><title>Jim Kinsey's Blog</title></head>
        <body>
          <header>
            <h1>Latest Blog Entries</h1>
          </header>
          <section id="blog-index">
            {
              entries.sortBy(_.lastModified.toEpochSecond()).reverse map { entry =>
                <article>
                  <header>
                    <h2><a href={"/blog/" + entry.id}>{entry.title}</a></h2>
                    <small><time>{entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))}</time></small>
                  </header>
                </article>
              }
            }
          </section>
          <footer>
            <small id="copyright">&copy; 2016-2017 Jim Kinsey</small>
          </footer>
        </body>
      </html>
    ))
  }

  def render(entry: BlogEntry): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(html(
      <html>
        <head><title>{entry.title}</title></head>
        <body>
          <article id="content">
            {html(entry.content.nodes)}
          </article>
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

object Renderer {
  sealed trait Failure
}