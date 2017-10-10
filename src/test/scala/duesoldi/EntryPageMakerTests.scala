package duesoldi.page

import bhuj.TemplateNotFound
import duesoldi.config.EnvironmentConfig
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.page.EntryPageMaker.{Failure, entryPage}
import duesoldi.page.EntryPageMaker.Failure.{EntryNotFound, InvalidId}
import duesoldi.rendering.Rendered
import duesoldi.storage.blog.Entry
import utest._
import duesoldi.test.matchers.CustomMatchers._

import scala.concurrent.Future

object EntryPageMakerTests 
  extends TestSuite 
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "an entry page maker" - {
      "produces a not found failure if the entry is not in the blog store" - {
        for {
          result <- entryPage(config)(noEntry)(rendersNothing)("123")
        } yield {
          assert(result isLeftOf EntryNotFound)
        }
      }
      "produces an invalid ID failure if the provided ID is not valid" - {
        for {
          result <- entryPage(config)(noEntry)(rendersNothing)("ASKJDKSADJH")
        } yield {
          assert(result isLeftOf InvalidId)
        }
      }
      "produces a render failure if the renderer fails" - {
        val failsToRender: Rendered = (_, _) => Future.successful(Left(bhuj.TemplateNotFound("foo")))
        for {
          result <- entryPage(config)(returnsEntry("hello"))(failsToRender)("hello")
        } yield {
          assert(result isLeftOf (Failure RenderFailure TemplateNotFound("foo")))
        }
      }
      "returns the result of rendering the entry" - {
        for {
          result <- entryPage(config)(returnsEntry("hello"))(renders("Rendered Page"))("hello")
        } yield {
          assert(result isRightOf "Rendered Page")
        }
      }
    }
  }
  private lazy val rendersNothing: Rendered = (_, _) => Future.successful(Right(""))
  private def renders(result: String): Rendered = (_, _) => Future.successful(Right(result))
  private lazy val config = EnvironmentConfig(Map.empty)
  private lazy val noEntry: Entry = _ => Future.successful(None)
  private def returnsEntry(name: String): Entry = _ => Future.successful(Some(BlogEntry("hello", MarkdownDocument.empty)))
}
