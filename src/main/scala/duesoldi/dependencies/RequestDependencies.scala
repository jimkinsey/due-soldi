package duesoldi.dependencies

import akka.http.scaladsl.model.HttpRequest
import duesoldi.config.Configured
import duesoldi.controller.BlogEntryRoutes
import duesoldi.page.{EntryPageMaker, EntryPageModel}
import duesoldi.validation.ValidIdentifier

import scala.concurrent.ExecutionContext

trait RequestDependencies { self: Configured with AppDependencies =>
  implicit def executionContext: ExecutionContext

  def withDependency[DEP]: ContextualDependencies.DependentBlock[DEP, HttpRequest] = ContextualDependencies.withDependency[DEP, HttpRequest]

  type ReqDep[DEP] = ContextualDependencies.ContextualDependency[DEP, HttpRequest]

  implicit lazy val makeEntryPage: ReqDep[BlogEntryRoutes.MakeEntryPage] =
    _ => EntryPageMaker.entryPage(ValidIdentifier.apply)(blogStore.entry)(EntryPageModel.pageModel(config))(renderer.render) _
}
