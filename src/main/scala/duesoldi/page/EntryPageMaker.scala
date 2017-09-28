package duesoldi.page

import java.time.format.DateTimeFormatter

import cats.data.EitherT
import duesoldi.config.Config
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.model.BlogEntry
import duesoldi.page.EntryPageFailure.{EntryNotFound, InvalidId, RenderFailure}
import duesoldi.rendering.{BlogEntryPageModel, Renderer}
import duesoldi.storage.BlogStore
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

sealed trait EntryPageFailure
object EntryPageFailure {
  case object InvalidId extends EntryPageFailure
  case object EntryNotFound extends EntryPageFailure
  case class RenderFailure(failure: Renderer.Failure) extends EntryPageFailure
}

class EntryPageMaker(renderer: Renderer, blogStore: BlogStore, config: Config)(implicit executionContext: ExecutionContext) {
  import cats.instances.all._

  def entryPage(entryId: String): Future[Either[EntryPageFailure, String]] = {
    (for {
      _ <- EitherT.fromEither[Future](validIdentifier(entryId))
      entry <- EitherT(blogEntry(entryId))
      model = pageModel(entry)
      html <- EitherT[Future, EntryPageFailure, String](render(model))
    } yield {
      html
    }).value
  }

  private def validIdentifier(entryId: String): Either[EntryPageFailure.InvalidId.type, String] = {
    ValidIdentifier(entryId).toRight({ InvalidId })
  }

  private def blogEntry(id: String): Future[Either[EntryPageFailure.EntryNotFound.type, BlogEntry]] = {
    blogStore.entry(id).map { _.toRight({ EntryNotFound }) }
  }

  private def pageModel(entry: BlogEntry) = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes).mkString,
    furnitureVersion = config.furnitureVersion
  )

  private def render(model: BlogEntryPageModel): Future[Either[RenderFailure, String]] = {
    renderer
      .render("blog-entry", model)
      .map(_.left.map(RenderFailure))
  }
}
