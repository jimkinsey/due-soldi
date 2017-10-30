package duesoldi.dependencies

import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.events.Events
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel}
import duesoldi.rendering.Renderer
import duesoldi.storage.JDBCBlogStore
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext
import Injection._
import duesoldi.logging.{EventLogging, Logger}

object DueSoldiDependencies
{
  implicit val logger: Inject[Logger] = {
    config =>
      new Logger(config.loggerName, config.loggingEnabled)
  }

  implicit val emit: Inject[duesoldi.events.Emit] = {
    config =>
      val events = new Events
      if (config.loggingEnabled) {
        EventLogging.enable(events, logger(config))
      }
      events emit _
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

  implicit val validIdentifier: Inject[duesoldi.validation.ValidIdentifier] = _ => ValidIdentifier.apply

  implicit val entryPageModel: Inject[EntryPageMaker.Model] = config => EntryPageModel.pageModel(config)

  implicit def makeEntryPage(implicit executionContext: ExecutionContext): Inject[MakeEntryPage] = {
    inject(EntryPageMaker.entryPage _)
  }
}
