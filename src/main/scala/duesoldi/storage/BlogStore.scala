package duesoldi.storage

import cats.data.EitherT
import duesoldi.markdown.MarkdownDocument.Heading
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
      entry    <- EitherT.fromEither[Future](blogEntry(name, parsed))
    } yield {
      entry
    }
    result.value
  }

  def entries: Future[Seq[BlogEntry]] = {
    source.documents map { _ flatMap { case (id, content) =>
      blogEntry(id, parser.markdown(content)).right.toOption
    } }
  }

  private def raw(name: String): EitherT[Future, NotFound.type, String] =
    EitherT[Future, NotFound.type, String](source.document(name).map(opt => opt.toRight({ NotFound })))

  private def blogEntry(id: String, document: MarkdownDocument): Either[BlogStore.Failure, BlogEntry] = {
    title(document) map { title => BlogEntry(id, title, document) } toRight { InvalidContent }
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