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
import scala.util.Try

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
      entries <- blogEntries
      model = BlogIndexPageModel(
        entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.flatMap {
          case BlogEntry(id, content, lastModified) =>
            Try(BlogIndexPageModel.Entry(
              lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
              title = MarkdownDocument.title(content).get,
              id = id
            )).toOption
        },
        furnitureVersion = config.furnitureVersion
      )
      html <- EitherT(renderer.render("blog-index", model)).leftMap(_.asInstanceOf[Any])
    } yield {
      html
    }).value map {
      case Right(html) => Right(html)
      case Left(IndexPageFailure.BlogStoreEmpty) => Left(IndexPageFailure.BlogStoreEmpty)
      case Left(failure: Renderer.Failure) => Left(IndexPageFailure.RenderFailure(failure))
      case Left(failure) => Left(IndexPageFailure.UnexpectedFailure(failure))
    }
  }

  private def blogEntries = EitherT[Future, IndexPageFailure.BlogStoreEmpty.type, Seq[BlogEntry]](blogStore.entries.map { entries =>
    entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
      case Nil => Left(IndexPageFailure.BlogStoreEmpty)
      case other => Right(other)
    }
  })

}
