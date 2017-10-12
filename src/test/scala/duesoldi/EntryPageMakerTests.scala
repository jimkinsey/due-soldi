package duesoldi.page

import bhuj.TemplateNotFound
import duesoldi.events.Event
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.page.EntryPageMaker.Failure.{EntryNotFound, InvalidId}
import duesoldi.page.EntryPageMaker.{Failure, Model, entryPage}
import duesoldi.rendering.{BlogEntryPageModel, Rendered}
import duesoldi.storage.blog.Entry
import duesoldi.test.matchers.CustomMatchers._
import utest._

import scala.collection.mutable
import scala.concurrent.Future

object EntryPageMakerTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "an entry page maker" - {
      "produces a not found failure if the entry is not in the blog store" - {
        for {
          result <- entryPage(validIdentifier)(noEntry)(returnsModel())(rendersNothing)("hello")
        } yield {
          assert(result isLeftOf EntryNotFound("hello"))
        }
      }
      "produces an invalid ID failure if the provided ID is not valid" - {
        for {
          result <- entryPage(invalidIdentifier)(noEntry)(returnsModel())(rendersNothing)("hello")
        } yield {
          assert(result isLeftOf InvalidId("hello"))
        }
      }
      "produces a render failure if the renderer fails" - {
        for {
          result <- entryPage(validIdentifier)(returnsEntry("hello"))(returnsModel())(failsToRender)("hello")
        } yield {
          assert(result isLeftOf (Failure RenderFailure TemplateNotFound("foo")))
        }
      }
      "returns the result of rendering the entry" - {
        for {
          result <- entryPage(validIdentifier)(returnsEntry("hello"))(returnsModel())(renders("Rendered Page"))("hello")
        } yield {
          assert(result isRightOf "Rendered Page")
        }
      }
      "emits a success event" - {
        val recorder = new EventRecorder
        implicit val emit: duesoldi.events.Emit = recorder.emit
        for {
          _ <- entryPage(validIdentifier)(returnsEntry("hello"))(returnsModel())(renders("html"))("hello")
        } yield {
          assert(recorder received EntryPageMaker.Event.MadePage("html"))
        }
      }
      "emits a failure event" - {
        val recorder = new EventRecorder
        implicit val emit: duesoldi.events.Emit = recorder.emit
        for {
          _ <- entryPage(invalidIdentifier)(returnsEntry("hello"))(returnsModel())(renders("html"))("hello")
        } yield {
          assert(recorder received EntryPageMaker.Event.FailedToMakePage(InvalidId("hello")))
        }
      }
    }
  }

  private lazy val validIdentifier: duesoldi.validation.ValidIdentifier = Option.apply
  private lazy val invalidIdentifier: duesoldi.validation.ValidIdentifier = _ => None
  private def returnsModel(): Model = _ => BlogEntryPageModel("title", "yesterday", "hello", "1")
  private lazy val rendersNothing: Rendered = (_, _) => Future.successful(Right(""))
  private lazy val failsToRender: Rendered = (_, _) => Future.successful(Left(bhuj.TemplateNotFound("foo")))
  private def renders(result: String): Rendered = (_, _) => Future.successful(Right(result))
  private lazy val noEntry: Entry = _ => Future.successful(None)
  private def returnsEntry(name: String): Entry = _ => Future.successful(Some(BlogEntry("hello", MarkdownDocument.empty)))
}

class EventRecorder {
  def emit(event: Event) = events.append(event)
  def received(event: Event) = events.contains(event)
  private lazy val events: mutable.Buffer[Event] = mutable.Buffer.empty
}