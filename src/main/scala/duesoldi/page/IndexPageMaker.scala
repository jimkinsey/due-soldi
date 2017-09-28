package duesoldi.page

import java.time.format.DateTimeFormatter

import cats.data.EitherT
import duesoldi.config.Config
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogIndexPageModel, Renderer}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

sealed trait IndexPageFailure
object IndexPageFailure {
  case object BlogStoreEmpty extends IndexPageFailure
  case class RenderFailure(failure: Renderer.Failure) extends IndexPageFailure
  case class UnexpectedFailure(failure: Any) extends IndexPageFailure
}

class IndexPageMaker(renderer: Renderer, blogStore: BlogStore, config: Config)(implicit executionContext: ExecutionContext) {
  import cats.instances.all._

  def indexPage: Future[Either[IndexPageFailure, String]] = {
    (for {
      entries <- EitherT(blogEntries)
      model = pageModel(entries)
      html <- EitherT(renderer.render("blog-index", model)).leftMap(_.asInstanceOf[Any])
    } yield {
      html
    }).leftMap {
      case IndexPageFailure.BlogStoreEmpty => IndexPageFailure.BlogStoreEmpty
      case failure: Renderer.Failure => IndexPageFailure.RenderFailure(failure)
      case failure => IndexPageFailure.UnexpectedFailure(failure)
    }.value
  }

  private def blogEntries: Future[Either[IndexPageFailure.BlogStoreEmpty.type, Seq[BlogEntry]]] =
    blogStore.entries.map { entries =>
      entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
        case Nil => Left(IndexPageFailure.BlogStoreEmpty)
        case other => Right(other)
      }
    }

  private def pageModel(entries: Seq[BlogEntry]) = BlogIndexPageModel(
    entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.map {
      case BlogEntry(id, content, lastModified) =>
        BlogIndexPageModel.Entry(
          lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
          title = MarkdownDocument.title(content).getOrElse("-untitled-"),
          id = id
        )
    },
    furnitureVersion = config.furnitureVersion
  )

}
