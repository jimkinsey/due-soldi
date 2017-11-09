package duesoldi.dependencies

import java.sql.ResultSet

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages._
import duesoldi.blog.storage._
import duesoldi.blog.validation.{ValidIdentifier, ValidateIdentifier}
import duesoldi.config.Config
import duesoldi.config.Config.Credentials
import duesoldi.controller.DebugRoutes.{MakeConfigPage, MakeHeadersPage}
import duesoldi.dependencies.Injection._
import duesoldi.events.Events
import duesoldi.furniture.{CurrentFurniturePath, Furniture}
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.markdown.MarkdownParser
import duesoldi.markdown.MarkdownParser.ParseMarkdown
import duesoldi.metrics.storage.{AccessRecordStorage, AccessRecordStore, GetAllAccessRecords}
import duesoldi.page.{ConfigPageMaker, _}
import duesoldi.rendering.Renderer
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.storage.JDBCConnection.{ConnectionDetails, PerformQuery, PerformUpdate}
import duesoldi.storage._

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
        AccessRecordStorage.enable(events, AccessRecordStore.put(jdbcPerformUpdate(config)))
      }
      events emit _
  }

  implicit val getAccessRecords: Inject[GetAllAccessRecords] = { config =>
    AccessRecordStore.getAll(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit def render(implicit executionContext: ExecutionContext): Inject[duesoldi.rendering.Render] = {
    inject(Renderer.render _)
  }

  implicit val validIdentifier: Inject[duesoldi.blog.validation.ValidateIdentifier] = _ => ValidIdentifier.apply

  implicit val entryPageModel: Inject[BuildEntryPageModel] = _ => EntryPageModel.pageModel

  implicit def makeEntryPage(implicit executionContext: ExecutionContext): Inject[MakeEntryPage] = {
    inject(EntryPageMaker.entryPage _)
  }

  implicit val indexPageModel:  Inject[BuildIndexPageModel] = _ => IndexPageModel.pageModel

  implicit def makeIndexPage(implicit executionContext: ExecutionContext): Inject[MakeIndexPage] = {
    inject(IndexPageMaker.makeIndexPage _)
  }

  implicit def makeHeadersPage: Inject[MakeHeadersPage] = _ => HeadersPageMaker.makeHeadersPage

  implicit def makeConfigPage: Inject[MakeConfigPage] = ConfigPageMaker.makeConfigPage

  implicit val adminCredentials: Inject[Credentials] = _.adminCredentials

  implicit lazy val config: Inject[Config] = config => config

  implicit val jdbcConnectionDetails: Inject[ConnectionDetails] = _.jdbcConnectionDetails

  implicit val jdbcPerformUpdate: Inject[PerformUpdate] = { config =>
    JDBCConnection.performUpdate(
      JDBCConnection.openConnection(config.jdbcConnectionDetails),
      JDBCConnection.prepareStatement,
      JDBCConnection.executeUpdate
    )
  }

  implicit def jdbcPerformQuery[T](implicit translate: ResultSet => T): Inject[PerformQuery[T]] = { config =>
    JDBCConnection.performQuery(
      JDBCConnection.openConnection(config.jdbcConnectionDetails),
      JDBCConnection.prepareStatement,
      JDBCConnection.executeQuery
    )
  }

  implicit val getBlogEntry: Inject[GetBlogEntry] = { config =>
    BlogStore.getOne(jdbcPerformQuery[BlogEntry](BlogStore.toBlogEntry(parseMarkdown(config)))(config))
  }

  implicit val getAllBlogEntries: Inject[GetAllBlogEntries] = { config =>
    BlogStore.getAll(jdbcPerformQuery[BlogEntry](BlogStore.toBlogEntry(parseMarkdown(config)))(config))
  }

  implicit val putBlogEntry: Inject[PutBlogEntry] = inject(BlogStore.put _)

  implicit val deleteBlogEntry: Inject[DeleteBlogEntry] = inject(BlogStore.delete _)

  implicit lazy val parseMarkdown: Inject[ParseMarkdown] = _ => MarkdownParser.parseMarkdown

  implicit lazy val currentFurniturePath: Inject[CurrentFurniturePath] = config => Furniture.currentPath(config.furniturePath)
}
