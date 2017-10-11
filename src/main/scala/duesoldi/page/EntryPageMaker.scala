package duesoldi.page

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
               (entryId: String)
               (implicit executionContext: ExecutionContext): Result = {
    for {
      _ <- validId(entryId).failWith({ InvalidId })
      entry <- entry(entryId).failWith({ EntryNotFound })
      model = pageModel(entry)
      html <- rendered("blog-entry", model).failWith[Failure](RenderFailure)
    } yield {
      html
    }
  }

  sealed trait Failure
  object Failure {
    case object InvalidId extends Failure
    case object EntryNotFound extends Failure
    case class RenderFailure(failure: bhuj.Failure) extends Failure
  }

  type Result = Future[Either[Failure, String]]

  type Model = (BlogEntry) => BlogEntryPageModel
}
