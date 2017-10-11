package duesoldi.controller

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi._
import duesoldi.config.Configured
import duesoldi.controller.BlogEntryRoutes.{MakeEntryPage, blogEntryRoutes}
import duesoldi.controller.Injection.{ContextualDependency, withDependency}
import duesoldi.events.Events
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.page.EntryPageMaker.Result
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
  lazy val makeEntryPage = EntryPageMaker.entryPage(ValidIdentifier.apply)(blogStore.entry)(EntryPageModel.pageModel(config))(renderer.render) _

//  import ContextualDependencies._

  implicit val mep: ContextualDependency[MakeEntryPage,HttpRequest] = (req) => makeEntryPage

  lazy val routes: Route =
    extractRequest { implicit request =>
      recordAccess {
        furnitureRoutes ~
        blogIndexRoutes ~
        withDependency[MakeEntryPage,HttpRequest](blogEntryRoutes) ~
        metricsRoutes ~
        robotsRoutes ~
        blogEditingRoutes ~
        debugRoutes
      }
    }


}

object Injection {
  type ContextualDependency[DEP,CTX] = CTX => DEP
  class DependentBlock[DEP,CTX] {
    def apply[RES](block: DEP => RES)(implicit dependency: ContextualDependency[DEP,CTX], context: CTX): RES = {
      block(dependency(context))
    }
  }
  def withDependency[DEP,CTX]: DependentBlock[DEP,CTX] = new DependentBlock[DEP,CTX]
}

object ContextualDependencies {
  implicit val mep: ContextualDependency[MakeEntryPage,HttpRequest] = (req) => ???
}