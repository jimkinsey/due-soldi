package duesoldi.dependencies

import java.sql.ResultSet

import dearboy.EventBus
import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages._
import duesoldi.blog.serialisation.EntryYaml
import duesoldi.blog.storage._
import duesoldi.blog.validation.{ValidBlogContent, ValidIdentifier}
import duesoldi.config.Config
import duesoldi.config.Config.Credentials
import duesoldi.debug.pages.{ConfigPageMaker, _}
import duesoldi.dependencies.Features.forFeature
import duesoldi.dependencies.Injection._
import duesoldi.furniture.CurrentPathAndContent
import duesoldi.furniture.storage.FurnitureFiles
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.{AccessRecordStorage, AccessRecordStore, GetAccessRecords, StoreAccessRecord}
import duesoldi.rendering.Renderer
import duesoldi.{Env, blog}
import hammerspace.markdown.MarkdownParser
import hammerspace.storage.JDBCConnection.{ConnectionDetails, PerformQuery, PerformUpdate}
import hammerspace.storage._

import scala.concurrent.ExecutionContext

object DueSoldiDependencies
{
  implicit val logger: Inject[Logger] = {
    config =>
      new Logger(config.loggerName, config.loggingEnabled)
  }

  implicit def emit(implicit executionContext: ExecutionContext): Inject[dearboy.Publish] = {
    config =>
      val events = new EventBus
      if (config.loggingEnabled) {
        EventLogging.enable(events, logger(config))
      }
      if (config.accessRecordingEnabled) {
        AccessRecordStorage.enable(events, AccessRecordStore.put(jdbcPerformUpdate(config)))
      }
      events publish _
  }

  implicit val getAccessRecords: Inject[GetAccessRecords] = { config =>
    AccessRecordStore.getAll(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val storeAccessRecord: Inject[StoreAccessRecord] = {
    inject(AccessRecordStore.put _)
  }

  implicit def render(implicit executionContext: ExecutionContext): Inject[duesoldi.rendering.Render] = {
    inject(Renderer.render _)
  }

  implicit val validateBlogIdentifier: Inject[duesoldi.blog.validation.ValidateIdentifier] = _ => ValidIdentifier.apply

  implicit val validateBlogContent: Inject[duesoldi.blog.validation.ValidateContent] = _ => ValidBlogContent.apply

  implicit val entryPageModel: Inject[BuildEntryPageModel] = inject(EntryPageModel.pageModel _)

  implicit val indexPageModel:  Inject[BuildIndexPageModel] = _ => IndexPageModel.pageModel

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

  implicit def putAllBlogEntries(implicit executionContext: ExecutionContext): Inject[PutBlogEntries] = inject(BlogStore.putAll _)

  implicit val deleteBlogEntry: Inject[DeleteBlogEntry] = inject(BlogStore.delete _)

  implicit val deleteAllBlogEntries: Inject[DeleteAllBlogEntries] = inject(BlogStore.deleteAll _)

  implicit lazy val parseMarkdown: Inject[hammerspace.markdown.Parse] = _ => MarkdownParser.parseMarkdown

  implicit lazy val furniturePathAndContent: Inject[CurrentPathAndContent] = _ => FurnitureFiles.currentPathAndContent

  implicit lazy val getBlogEntryTwitterCard: Inject[GetEntryTwitterMetadata] = {
    forFeature("TWITTER_CARDS")(ifOn = BlogEntryTwitterMetadata.getTwitterCard, ifOff = BlogEntryTwitterMetadata.noTwitterCard)
  }

  implicit lazy val blogEntryFromYaml: Inject[blog.EntryFromYaml] = _ => EntryYaml.parse

  implicit lazy val blogEntryToYaml: Inject[blog.EntryToYaml] = _ => EntryYaml.format

  implicit lazy val blogEntriesToYaml: Inject[blog.EntriesToYaml] = _ => EntryYaml.formatAll

  implicit lazy val blogEntriesFromYaml: Inject[blog.EntriesFromYaml] = _ => EntryYaml.parseAll
}

object Features
{
  def forFeature[T](name: String)(ifOn: => T, ifOff: => T): Inject[T] = (config) => {
    config.features.get(name) match {
      case Some(true) => ifOn
      case _ => ifOff
    }
  }

  def featureStatuses(env: Env): Map[String,Boolean] = {
    env.collect {
      case (key, value) if key.startsWith("FEATURE_") => key.substring("FEATURE_".length) -> (value == "on")
    }
  }
}
