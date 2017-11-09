package duesoldi.page

import duesoldi.model.BlogEntry
import duesoldi.rendering.{BlogIndexPageModel, Render}
import duesoldi.storage.blog.GetAllBlogEntries
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}

object IndexPageMaker
{
  import Failure._
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def makeIndexPage(pageModel: Model, rendered: Render, getEntries: GetAllBlogEntries)
                   (implicit executionContext: ExecutionContext): () => Result = { () =>
    for {
      entries <- blogEntries(getEntries).propagate[Failure]
      model = pageModel(entries)
      html <- rendered("blog-index", model).failWith[Failure](RenderFailure)
    } yield {
      html
    }
  }

  private def blogEntries(getEntries: GetAllBlogEntries)
                         (implicit executionContext: ExecutionContext): Future[Either[Failure.BlogStoreEmpty.type, Seq[BlogEntry]]] =
    getEntries().map { entries =>
      entries.filter(entry => ValidIdentifier(entry.id).nonEmpty) match {
        case Nil => Left(Failure.BlogStoreEmpty)
        case other => Right(other)
      }
    }

  sealed trait Failure
  object Failure {
    case object BlogStoreEmpty extends Failure
    case class RenderFailure(failure: bhuj.Failure) extends Failure
  }

  type Result = Future[Either[Failure, String]]

  type Model = Seq[BlogEntry] => BlogIndexPageModel
}
