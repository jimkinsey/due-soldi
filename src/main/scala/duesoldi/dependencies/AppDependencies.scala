package duesoldi.dependencies

import duesoldi.config.Config
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.IndexPageMaker
import duesoldi.rendering.Renderer
import duesoldi.storage.{JDBCAccessRecordStore, JDBCBlogStore}

import scala.concurrent.ExecutionContext

class AppDependencies(val config: Config)(implicit val executionContext: ExecutionContext) {
  lazy val events = new Events
  lazy val logger = new Logger("App", config.loggingEnabled)
  lazy val eventLogging = new EventLogging(events, logger)
  lazy val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  lazy val indexPageMaker = new IndexPageMaker(renderer.render, blogStore, config)
}
