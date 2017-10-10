package duesoldi.page

import java.time.format.DateTimeFormatter

import duesoldi.config.Config
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.page.IndexPageFailure.RenderFailure
import duesoldi.rendering.{BlogIndexPageModel, Rendered}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

sealed trait IndexPageFailure
object IndexPageFailure {
  case object BlogStoreEmpty extends IndexPageFailure
  case class RenderFailure(failure: bhuj.Failure) extends IndexPageFailure
}

class IndexPageMaker(rendered: Rendered, blogStore: BlogStore, config: Config)(implicit executionContext: ExecutionContext) {
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def indexPage: Future[Either[IndexPageFailure, String]] = {
    for {
      entries <- blogEntries.propagate[IndexPageFailure]
      model = pageModel(entries)
      html <- rendered("blog-index", model).failWith[IndexPageFailure](RenderFailure)
    } yield {
      html
    }
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
