package duesoldi.dependencies

import duesoldi.config.Config
import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.controller.RequestContext
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker}
import duesoldi.rendering.Renderer
import duesoldi.storage.{JDBCAccessRecordStore, JDBCBlogStore}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

class RequestDependencies(config: Config)(implicit executionContext: ExecutionContext, context: RequestContext) {
  implicit lazy val emit: duesoldi.events.Emit = events emit _
  lazy val events = new Events
  lazy val eventLogging = new EventLogging(events, logger)
  lazy val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  lazy val makeEntryPage: MakeEntryPage = EntryPageMaker.entryPage(ValidIdentifier.apply)(blogStore.entry)(EntryPageModel.pageModel(config))(renderer.render)(executionContext, emit)
  lazy val indexPageMaker: IndexPageMaker = new IndexPageMaker(renderer.render, blogStore, config)
  lazy val logger = new Logger(s"Request ${context.id}")

  new EventLogging(events, logger)
}
