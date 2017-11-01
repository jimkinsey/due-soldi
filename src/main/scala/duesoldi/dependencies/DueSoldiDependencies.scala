package duesoldi.dependencies

import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.events.Events
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker}
import duesoldi.rendering.Renderer
import duesoldi.storage.{AccessRecordStorage, AccessRecordStore, JDBCAccessRecordStore, JDBCBlogStore}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.{ExecutionContext, Future}
import Injection._
import duesoldi.logging.{EventLogging, Logger}

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
    val renderer = new Renderer
    _ => renderer.render
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

  implicit val entryPageModel: Inject[EntryPageMaker.Model] = config => EntryPageModel.pageModel(config)

  implicit def makeEntryPage(implicit executionContext: ExecutionContext): Inject[MakeEntryPage] = {
    inject(EntryPageMaker.entryPage _)
  }

  implicit def indexPageMaker(implicit executionContext: ExecutionContext): Inject[IndexPageMaker] = {
    config =>
      new IndexPageMaker(render(executionContext)(config), blogStore(executionContext)(config), config)
  }
}
