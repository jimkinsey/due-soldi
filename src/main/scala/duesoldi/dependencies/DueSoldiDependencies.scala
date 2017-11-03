package duesoldi.dependencies

import duesoldi.config.Config
import duesoldi.config.Config.Credentials
import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.controller.BlogIndexRoutes.MakeIndexPage
import duesoldi.controller.DebugRoutes.{MakeConfigPage, MakeHeadersPage}
import duesoldi.controller.{ConfigPageMaker, HeadersPageMaker}
import duesoldi.dependencies.Injection._
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker, IndexPageModel}
import duesoldi.rendering.Renderer
import duesoldi.storage.{AccessRecordStorage, AccessRecordStore, JDBCAccessRecordStore, JDBCBlogStore}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

object DueSoldiDependencies
{
  implicit val logger: Inject[Logger] = {
    config =>
      new Logger(config.loggerName, config.loggingEnabled)
  }

  implicit def emit(implicit executionContext: ExecutionContext): Inject[duesoldi.events.Emit] = {
    config =>
      val events = new Events
      if (config.loggingEnabled) {
        EventLogging.enable(events, logger(config))
      }
      if (config.accessRecordingEnabled) {
        AccessRecordStorage.enable(events, accessRecordStore(executionContext)(config))
      }
      events emit _
  }

  implicit def accessRecordStore(implicit executionContext: ExecutionContext): Inject[AccessRecordStore] = {
    config =>
      new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  }

  implicit def render(implicit executionContext: ExecutionContext): Inject[duesoldi.rendering.Rendered] = {
    _ => Renderer.render
  }

  implicit def entry(implicit executionContext: ExecutionContext): Inject[duesoldi.storage.blog.Entry] = {
    config =>
      val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
      blogStore.entry
  }

  implicit def blogStore(implicit executionContext: ExecutionContext): Inject[duesoldi.storage.BlogStore] = {
    config =>
      new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  }

  implicit val validIdentifier: Inject[duesoldi.validation.ValidIdentifier] = _ => ValidIdentifier.apply

  implicit val entryPageModel: Inject[EntryPageMaker.Model] = EntryPageModel.pageModel

  implicit def makeEntryPage(implicit executionContext: ExecutionContext): Inject[MakeEntryPage] = {
    inject(EntryPageMaker.entryPage _)
  }

  implicit val indexPageModel:  Inject[IndexPageMaker.Model] = IndexPageModel.pageModel

  implicit def makeIndexPage(implicit executionContext: ExecutionContext): Inject[MakeIndexPage] = {
    inject(IndexPageMaker.makeIndexPage _)
  }

  implicit def makeHeadersPage: Inject[MakeHeadersPage] = _ => HeadersPageMaker.makeHeadersPage

  implicit def makeConfigPage: Inject[MakeConfigPage] = ConfigPageMaker.makeConfigPage

  implicit val adminCredentials: Inject[Credentials] = _.adminCredentials

  implicit lazy val config: Inject[Config] = config => config

  implicit val markdownParser: Inject[MarkdownParser] = _ => new MarkdownParser
}
