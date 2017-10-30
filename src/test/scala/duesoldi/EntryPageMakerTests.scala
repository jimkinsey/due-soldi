package duesoldi.page

import bhuj.TemplateNotFound
import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.events.Emit
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.page.EntryPageMaker.Failure.{EntryNotFound, InvalidId}
import duesoldi.page.EntryPageMaker.{Failure, Model, entryPage}
import duesoldi.rendering.{BlogEntryPageModel, Rendered}
import duesoldi.storage.blog.Entry
import duesoldi.test.matchers.CustomMatchers._
import duesoldi.validation.ValidIdentifier
import utest._

import scala.collection.mutable
import scala.concurrent.Future

object EntryPageMakerTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow

  def withEntryPageMaker[T](
    validIdentifier: ValidIdentifier = validatesIdentifier,
    entry: Entry = returnsEntry("test"),
    model: Model = returnsModel(),
    rendered: Rendered = renders("some html")
  )(block: (MakeEntryPage, EventRecorder) => T): T = {
    val recorder = new EventRecorder
    val emit: Emit = recorder.emit
    block(entryPage(validIdentifier, entry, model, rendered, emit), recorder)
  }

  val tests = this {
    "an entry page maker" - {
      "produces a not found failure if the entry is not in the blog store" - {
        withEntryPageMaker(entry = noEntry) { (page, _) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf EntryNotFound("hello"))
          }
        }
      }
      "produces an invalid ID failure if the provided ID is not valid" - {
        withEntryPageMaker(validIdentifier = invalidIdentifier) { (page, _) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf InvalidId("hello"))
          }
        }
      }
      "produces a render failure if the renderer fails" - {
        withEntryPageMaker(entry = returnsEntry("hello"), rendered = failsToRender) { (page, _) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf (Failure RenderFailure TemplateNotFound("foo")))
          }
        }
      }
      "returns the result of rendering the entry" - {
        withEntryPageMaker(entry = returnsEntry("hello"), rendered = renders("Rendered Page")) { (page, _) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isRightOf "Rendered Page")
          }
        }
      }
      "emits a success event" - {
        withEntryPageMaker(entry = returnsEntry("hello"), rendered = renders("Rendered Page")) { (page, recorder) =>
          for {
            _ <- page("hello")
          } yield {
            assert(recorder received EntryPageMaker.Event.MadePage("Rendered Page"))
          }
        }
      }
      "emits a failure event" - {
        withEntryPageMaker(validIdentifier = invalidIdentifier) { (page, recorder) =>
          for {
            _ <- page("hello")
          } yield {
            assert(recorder received EntryPageMaker.Event.FailedToMakePage(InvalidId("hello")))
          }
        }
      }
    }
  }

  private lazy val validatesIdentifier: duesoldi.validation.ValidIdentifier = Option.apply
  private lazy val invalidIdentifier: duesoldi.validation.ValidIdentifier = _ => None
  private def returnsModel(): Model = _ => BlogEntryPageModel("title", "yesterday", "hello", "1")
  private lazy val rendersNothing: Rendered = (_, _) => Future.successful(Right(""))
  private lazy val failsToRender: Rendered = (_, _) => Future.successful(Left(bhuj.TemplateNotFound("foo")))
  private def renders(result: String): Rendered = (_, _) => Future.successful(Right(result))
  private lazy val noEntry: Entry = _ => Future.successful(None)
  private def returnsEntry(name: String): Entry = _ => Future.successful(Some(BlogEntry("hello", MarkdownDocument.empty)))
}

class EventRecorder {
  def emit(event: Any) = events.append(event)
  def received(event: Any) = events.contains(event)
  private lazy val events: mutable.Buffer[Any] = mutable.Buffer.empty
}