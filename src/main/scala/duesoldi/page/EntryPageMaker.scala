package duesoldi.page

import java.time.format.DateTimeFormatter

import duesoldi.config.Config
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.model.BlogEntry
import duesoldi.page.EntryPageMaker.Failure.{EntryNotFound, InvalidId, RenderFailure}
import duesoldi.rendering.{BlogEntryPageModel, PageModel}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

object EntryPageMaker {
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def entryPage(config: Config)
               (entry: duesoldi.storage.blog.Entry)
               (rendered: duesoldi.rendering.Rendered)
               (entryId: String)
               (implicit executionContext: ExecutionContext): Result = {
    for {
      _ <- ValidIdentifier(entryId).failWith({ InvalidId })
      entry <- entry(entryId).failWith({ EntryNotFound })
      model = pageModel(entry, config)
      html <- rendered("blog-entry", model).failWith[Failure](RenderFailure)
    } yield {
      html
    }
  }

  private def pageModel(entry: BlogEntry, config: Config) = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes).mkString,
    furnitureVersion = config.furnitureVersion
  )

  sealed trait Failure
  object Failure {
    case object InvalidId extends Failure
    case object EntryNotFound extends Failure
    case class RenderFailure(failure: bhuj.Failure) extends Failure
  }

  type Result = Future[Either[Failure, String]]
}
