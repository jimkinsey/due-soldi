package duesoldi.rendering

import duesoldi.markdown.MarkdownDocument
import duesoldi.markdown.MarkdownDocument._
import duesoldi.model.BlogEntry

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq, XML}

// TODO leave the h1 out of the content? or ditch the explicit h1?
// TODO article tag
class Renderer {
  def render(entry: BlogEntry)(implicit ec: ExecutionContext): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(html(
      <html>
        <head><title>{entry.title}</title></head>
        <body>
          <h1>{entry.title}</h1>
          <div id="content">
            {html(entry.content.nodes)}
          </div>
        </body>
      </html>
    ))
  }

  private def html(xml: Elem): String = s"<!DOCTYPE html>n${xml.mkString}\n"

  private def html(nodes: Seq[MarkdownDocument.Node]): NodeSeq = {
    nodes collect {
      case Heading(text, level) => XML.loadString(s"<h$level>$text</h$level>")
      case Paragraph(content)   => <p>{html(content)}</p>
      case Text(content)        => <_>{content}</_>.child.head
      case Emphasis(content)    => <i>{content}</i>
      case Strong(content)      => <b>{content}</b>
    }
  }
}

object Renderer {
  sealed trait Failure
}