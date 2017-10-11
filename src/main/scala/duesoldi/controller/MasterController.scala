package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi._
import duesoldi.config.Configured
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker}
import duesoldi.rendering.Renderer
import duesoldi.storage._
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Controller
  with Configured
  with AccessRecording
  with FurnitureRoutes
  with MetricsRoutes
  with BlogIndexRoutes
  with BlogEntryRoutes
  with RobotsRoutes
  with BlogEditingRoutes
  with DebugRoutes {

  lazy val events = new Events
  lazy val logger = new Logger("Master Controller", config.loggingEnabled)
  lazy val logging = new EventLogging(events, logger)
  lazy val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  lazy val indexPageMaker = new IndexPageMaker(renderer.render, blogStore, config)
  lazy val entryPage = EntryPageMaker.entryPage(ValidIdentifier.apply)(blogStore.entry)(EntryPageModel.pageModel(config))(renderer.render)

  lazy val routes: Route =
    recordAccess {
      furnitureRoutes ~
      blogIndexRoutes ~
      blogEntryRoutes ~
      metricsRoutes ~
      robotsRoutes ~
      blogEditingRoutes ~
      debugRoutes
    }

}
