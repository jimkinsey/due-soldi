package duesoldi.storage

import cats.data.EitherT
import duesoldi.markdown.MarkdownDocument.{Container, Heading, Node, TextNode}
import duesoldi.markdown.{MarkdownDocument, MarkdownParser}
import duesoldi.model.BlogEntry
import duesoldi.storage.BlogStore.{InvalidContent, NotFound}

import scala.concurrent.{ExecutionContext, Future}

class BlogStore(source: MarkdownSource, parser: MarkdownParser)(implicit ec: ExecutionContext) {

  import cats.instances.all._

  def entry(name: String): Future[Either[BlogStore.Failure, BlogEntry]] = {
    val result = for {
      document <- raw(name)
      parsed   = parser.markdown(document)
      entry    <- blogEntry(parsed)
    } yield {
      entry
    }
    result.value
  }

  private def raw(name: String): EitherT[Future, NotFound.type, String] =
    EitherT[Future, NotFound.type, String](source.document(name).map(opt => opt.toRight({ NotFound })))

  private def blogEntry(document: MarkdownDocument): EitherT[Future, BlogStore.Failure, BlogEntry] = {
    EitherT.fromOption[Future](title(document) map { title => BlogEntry(title, document) }, { InvalidContent })
  }

  private def title(markdown: MarkdownDocument): Option[String] = markdown.nodes.collectFirst {
    case Heading(nodes, 1) => MarkdownDocument.text(nodes)
  }

}

object BlogStore {
  sealed trait Failure
  case object NotFound extends Failure
  case object InvalidContent extends Failure
}