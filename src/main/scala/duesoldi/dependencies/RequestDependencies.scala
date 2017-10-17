package duesoldi.dependencies

import duesoldi.controller.BlogEntryRoutes.MakeEntryPage
import duesoldi.controller.RequestContext
import duesoldi.events.{Event, Events}
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.page.{EntryPageMaker, EntryPageModel, IndexPageMaker}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

class RequestDependencies(appDependencies: AppDependencies, context: RequestContext)(implicit executionContext: ExecutionContext) {
  import appDependencies.{blogStore, renderer, config}
  implicit lazy val emit: duesoldi.events.Emit = events emit _

  lazy val makeEntryPage: MakeEntryPage = EntryPageMaker.entryPage(ValidIdentifier.apply)(blogStore.entry)(EntryPageModel.pageModel(config))(renderer.render)(executionContext, emit)
  lazy val indexPageMaker: IndexPageMaker = new IndexPageMaker(renderer.render, blogStore, config)

  lazy val events = new Events

  private lazy val logger = new Logger(s"Request ${context.id}")
  new EventLogging(events, logger)
}
