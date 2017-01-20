package duesoldi.rendering

import duesoldi.markdown.MarkdownDocument
import duesoldi.markdown.MarkdownDocument._
import duesoldi.model.BlogEntry

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

class Renderer {
  def render(entry: BlogEntry)(implicit ec: ExecutionContext): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(
      <html>
        <head><title>{entry.title}</title></head>
        <body>
          <h1>{entry.title}</h1>
          <div id="content">
            {html(entry.content.nodes)}
          </div>
        </body>
      </html>.mkString
    )
  }

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