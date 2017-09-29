package duesoldi.page

import java.time.format.DateTimeFormatter

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
  import duesoldi.transformers.TransformerOps._

  def entryPage(entryId: String): Future[Either[EntryPageFailure, String]] = {
    for {
      _ <- ValidIdentifier(entryId).failWith({ InvalidId })
      entry <- blogStore.entry(entryId).failWith({ EntryNotFound })
      model = pageModel(entry)
      html <- renderer.render("blog-entry", model).failWith[EntryPageFailure](RenderFailure)
    } yield {
      html
    }
  }

  private def pageModel(entry: BlogEntry) = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes).mkString,
    furnitureVersion = config.furnitureVersion
  )
}
