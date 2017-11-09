package duesoldi.test.unit

import bhuj.TemplateNotFound
import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages.EntryPageMaker.Failure
import duesoldi.blog.pages.EntryPageMaker.Failure.{EntryNotFound, InvalidId}
import duesoldi.blog.pages.{BlogEntryPageModel, BuildEntryPageModel, EntryPageMaker, MakeEntryPage}
import duesoldi.blog.storage.GetBlogEntry
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.events
import duesoldi.events.Emit
import duesoldi.markdown.MarkdownDocument
import duesoldi.rendering.Render
import duesoldi.test.support.events.EventRecording._
import duesoldi.test.support.matchers.CustomMatchers._
import utest._

import scala.concurrent.Future

object EntryPageMakerTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "an entry page maker" - {
      "produces a not found failure if the entry is not in the blog store" - {
        withEntryPageMaker(entry = noEntry) { (page) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf EntryNotFound("hello"))
          }
        }
      }
      "produces an invalid ID failure if the provided ID is not valid" - {
        withEntryPageMaker(validIdentifier = invalidIdentifier) { (page) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf InvalidId("hello"))
          }
        }
      }
      "produces a render failure if the renderer fails" - {
        withEntryPageMaker(entry = returnsEntry("hello"), rendered = failsToRender) { (page) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isLeftOf (Failure RenderFailure TemplateNotFound("foo")))
          }
        }
      }
      "returns the result of rendering the entry" - {
        withEntryPageMaker(entry = returnsEntry("hello"), rendered = renders("Rendered Page")) { (page) =>
          for {
            result <- page("hello")
          } yield {
            assert(result isRightOf "Rendered Page")
          }
        }
      }
      "emits a success event" - {
        withRecorder { recorder =>
          withEntryPageMaker(entry = returnsEntry("hello"), rendered = renders("Rendered Page"), emit = recorder.emit) { (page) =>
            for {
              _ <- page("hello")
            } yield {
              assert(recorder received EntryPageMaker.Event.MadePage("Rendered Page"))
            }
          }
        }
      }
      "emits a failure event" - {
        withRecorder { recorder =>
          withEntryPageMaker(validIdentifier = invalidIdentifier, emit = recorder.emit) { (page) =>
            for {
              _ <- page("hello")
            } yield {
              assert(recorder received EntryPageMaker.Event.FailedToMakePage(InvalidId("hello")))
            }
          }
        }
      }
    }
  }

  private lazy val validatesIdentifier: ValidateIdentifier = _ => None
  private lazy val invalidIdentifier: ValidateIdentifier = _ => Some("Invalid identifier")
  private def returnsModel(): BuildEntryPageModel = _ => BlogEntryPageModel("title", "yesterday", "hello")
  private lazy val rendersNothing: Render = (_, _) => Future.successful(Right(""))
  private lazy val failsToRender: Render = (_, _) => Future.successful(Left(bhuj.TemplateNotFound("foo")))
  private def renders(result: String): Render = (_, _) => Future.successful(Right(result))
  private lazy val noEntry: GetBlogEntry = _ => Future.successful(None)
  private def returnsEntry(name: String): GetBlogEntry = _ => Future.successful(Some(BlogEntry("hello", MarkdownDocument.empty)))

  private def withEntryPageMaker[T](
                                     validIdentifier: ValidateIdentifier = validatesIdentifier,
                                     entry: GetBlogEntry = returnsEntry("test"),
                                     model: BuildEntryPageModel = returnsModel(),
                                     rendered: Render = renders("some html"),
                                     emit: Emit = events.noopEmit
  )(block: MakeEntryPage => T): T = {
    block(EntryPageMaker.entryPage(validIdentifier, entry, model, rendered, emit))
  }
}
