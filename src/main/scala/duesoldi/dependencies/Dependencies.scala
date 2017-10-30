package duesoldi.dependencies

import duesoldi.config.Config
import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker}
import duesoldi.rendering.Renderer
import duesoldi.storage.{AccessRecordStorage, JDBCAccessRecordStore, JDBCBlogStore}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

class Dependencies(config: Config)(implicit executionContext: ExecutionContext)
{
  implicit lazy val emit: duesoldi.events.Emit = events emit _
  implicit lazy val events = new Events
  implicit lazy val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  implicit lazy val renderer = new Renderer
  implicit lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  implicit lazy val indexPageMaker: IndexPageMaker = new IndexPageMaker(renderer.render, blogStore, config)
  implicit lazy val logger = new Logger(config.loggerName)

  if (config.loggingEnabled) {
    EventLogging.enable(events, logger)
  }

  if (config.accessRecordingEnabled) {
    AccessRecordStorage.enable(events, accessRecordStore)
  }
}
