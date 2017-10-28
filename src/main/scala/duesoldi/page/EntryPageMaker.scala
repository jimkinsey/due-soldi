package duesoldi.page

import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.model.BlogEntry
import duesoldi.rendering.BlogEntryPageModel

import scala.concurrent.{ExecutionContext, Future}

object EntryPageMaker {
  import Failure._
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def entryPage(validId: duesoldi.validation.ValidIdentifier)
               (entry: duesoldi.storage.blog.Entry)
               (pageModel: Model)
               (rendered: duesoldi.rendering.Rendered)
               (implicit executionContext: ExecutionContext, emit: duesoldi.events.Emit = duesoldi.events.noopEmit): MakeEntryPage = { entryId: String =>
    (for {
      _ <- validId(entryId).failWith({ InvalidId(entryId) })
      entry <- entry(entryId).failWith({ EntryNotFound(entryId) })
      model = pageModel(entry)
      html <- rendered("blog-entry", model).failWith[Failure](RenderFailure)
    } yield {
      html
    })
      .onRight(html => emit(Event.MadePage(html)))
      .onLeft(failure => emit(Event.FailedToMakePage(failure)))
  }

  sealed trait Failure
  object Failure {
    case class InvalidId(id: String) extends Failure
    case class EntryNotFound(id: String) extends Failure
    case class RenderFailure(failure: bhuj.Failure) extends Failure
  }

  sealed trait Event
  object Event {
    case class MadePage(html: String) extends Event
    case class FailedToMakePage(failure: Failure) extends Event
  }

  type Result = Future[Either[Failure, String]]

  type Model = (BlogEntry) => BlogEntryPageModel
}
