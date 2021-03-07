package duesoldi.dependencies

import java.sql.ResultSet
import dearboy.EventBus
import duesoldi.app.sessions.Sessions
import duesoldi.app.sessions.Sessions.GetSessionCookie
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
import duesoldi.gallery.ArtworkPageModel
import duesoldi.gallery.model.Artwork
import duesoldi.gallery.pages.BuildArtworkPageModel
import duesoldi.gallery.serialisation.ArtworkYaml
import duesoldi.gallery.storage.{CreateOrUpdateArtwork, DeleteAllArtworks, DeleteArtwork, GalleryStore, GetAllArtworks, GetArtwork, PutArtwork, PutArtworks}
import duesoldi.logging.{EventLogging, Logger}
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage._
import duesoldi.rendering.{GetTemplate, Renderer}
import duesoldi.{Env, blog, gallery}
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

  implicit lazy val getAccessRecordsWithCount: Inject[GetAccessRecordsWithCount] = { config =>
    AccessRecordStore.getAllWithCount(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val getAccessRecordLogSize: Inject[GetAccessRecordLogSize] = { config =>
    AccessRecordStore.getLogSize(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val deleteAccessRecord: Inject[DeleteAccessRecord] = inject(AccessRecordStore.delete _)

  implicit lazy val updateAccessRecord: Inject[UpdateAccessRecord] = inject(AccessRecordStore.update _)

  implicit lazy val getAccessRecords: Inject[GetAccessRecords] = { config =>
    AccessRecordStore.getAll(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val storeAccessRecord: Inject[StoreAccessRecord] = inject(AccessRecordStore.put _)

  implicit lazy val storeAccessRecordArchive: Inject[StoreAccessRecordArchive] = inject(AccessRecordArchiveStore.put _)

  implicit lazy val getAccessRecordArchive: Inject[GetAccessRecordArchive] = config => {
    AccessRecordArchiveStore.get(jdbcPerformQuery(AccessRecordArchiveStore.toArchive)(config))
  }

  implicit lazy val deleteAccessRecordArchive: Inject[DeleteAccessRecordArchive] = inject(AccessRecordArchiveStore.delete _)

  implicit def getAllAccessRecords(implicit executionContext: ExecutionContext): Inject[GetAllAccessRecords] = config => {
    AccessRecordStorage.getIncludingArchived(getAccessRecords(config), getAccessRecordArchive(config))
  }

  implicit lazy val getTemplate: Inject[GetTemplate] = config => {
    config.templatePath match {
      case Some(path) =>
        injected[Logger](logger, config).info(s"Loading mustache templates from '$path'")
        Renderer.getTemplateFromPath(path)
      case _ => Renderer.getTemplateFromResources
    }
  }

  implicit def render(implicit executionContext: ExecutionContext): Inject[duesoldi.rendering.Render] = {
    inject(Renderer.render _)
  }

  implicit val validateBlogIdentifier: Inject[duesoldi.blog.validation.ValidateIdentifier] = _ => ValidIdentifier.apply

  implicit val validateBlogContent: Inject[duesoldi.blog.validation.ValidateContent] = _ => ValidBlogContent.apply

  implicit val entryPageModel: Inject[BuildEntryPageModel] = inject(EntryPageModel.pageModel _)

  implicit val artworkPageModel: Inject[BuildArtworkPageModel] = inject(ArtworkPageModel.build _)

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

  implicit val getArtwork: Inject[GetArtwork] = { config =>
    GalleryStore.getOne(jdbcPerformQuery[Artwork](GalleryStore.toArtwork(parseMarkdown(config)))(config))
  }

  implicit val getAllBlogEntries: Inject[GetAllBlogEntries] = { config =>
    BlogStore.getAll(jdbcPerformQuery[BlogEntry](BlogStore.toBlogEntry(parseMarkdown(config)))(config))
  }

  implicit val getAllArtworks: Inject[GetAllArtworks] = { config =>
    GalleryStore.getAll(jdbcPerformQuery[Artwork](GalleryStore.toArtwork(parseMarkdown(config)))(config))
  }

  implicit val putBlogEntry: Inject[PutBlogEntry] = inject(BlogStore.put _)

  implicit val putArtwork: Inject[PutArtwork] = inject(GalleryStore.put _)

  implicit def putAllBlogEntries(implicit executionContext: ExecutionContext): Inject[PutBlogEntries] = inject(BlogStore.putAll _)

  implicit def putAllArtworks(implicit executionContext: ExecutionContext): Inject[PutArtworks] = inject(GalleryStore.putAll _)

  implicit val deleteBlogEntry: Inject[DeleteBlogEntry] = inject(BlogStore.delete _)

  implicit val deleteArtwork: Inject[DeleteArtwork] = inject(GalleryStore.delete _)

  implicit val deleteAllBlogEntries: Inject[DeleteAllBlogEntries] = inject(BlogStore.deleteAll _)

  implicit val deleteAllArtworks: Inject[DeleteAllArtworks] = inject(GalleryStore.deleteAll _)

  implicit def createOrUpdateBlogEntry(implicit executionContext: ExecutionContext): Inject[CreateOrUpdateBlogEntry] = inject(BlogStore.createOrUpdate _)

  implicit def createOrUpdateArtwork(implicit executionContext: ExecutionContext): Inject[CreateOrUpdateArtwork] = inject(GalleryStore.createOrUpdate _)

  implicit lazy val parseMarkdown: Inject[hammerspace.markdown.Parse] = _ => MarkdownParser.parseMarkdown

  implicit lazy val furniturePathAndContent: Inject[CurrentPathAndContent] = _ => FurnitureFiles.currentPathAndContent

  implicit lazy val getBlogEntryTwitterCard: Inject[GetEntryTwitterMetadata] = {
    forFeature("TWITTER_CARDS")(ifOn = BlogEntryTwitterMetadata.getTwitterCard, ifOff = BlogEntryTwitterMetadata.noTwitterCard)
  }

  implicit lazy val blogEntryFromYaml: Inject[blog.EntryFromYaml] = _ => EntryYaml.parse

  implicit lazy val artworkFromYaml: Inject[gallery.ArtworkFromYaml] = _ => ArtworkYaml.parse

  implicit lazy val blogEntryToYaml: Inject[blog.EntryToYaml] = _ => EntryYaml.format

  implicit lazy val artworkToYaml: Inject[gallery.ArtworkToYaml] = _ => ArtworkYaml.format

  implicit lazy val blogEntriesToYaml: Inject[blog.EntriesToYaml] = _ => EntryYaml.formatAll

  implicit lazy val artworksToYaml: Inject[gallery.ArtworksToYaml] = _ => ArtworkYaml.formatAll

  implicit lazy val blogEntriesFromYaml: Inject[blog.EntriesFromYaml] = _ => EntryYaml.parseAll

  implicit lazy val artworksFromYaml: Inject[gallery.ArtworksFromYaml] = _ => ArtworkYaml.parseAll

  implicit lazy val getSessionCookie: Inject[GetSessionCookie] = config => Sessions.getSessionCookie(config.secretKey)
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
