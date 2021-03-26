package duesoldi.blog.dependencies

import duesoldi.blog
import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages._
import duesoldi.blog.serialisation.EntryYaml
import duesoldi.blog.storage._
import duesoldi.blog.validation.{ValidBlogContent, ValidIdentifier}
import duesoldi.dependencies.Features.forFeature
import duesoldi.dependencies.Injection.{Inject, inject}
import duesoldi.dependencies.{FurnitureDependencies, JDBCDependencies, MarkdownParsingDependencies, RenderingDependencies, SessionCookieDependencies}

import scala.concurrent.ExecutionContext

trait BlogDependencies
extends JDBCDependencies
with MarkdownParsingDependencies
with RenderingDependencies
with SessionCookieDependencies {

  implicit lazy val getBlogEntryTwitterCard: Inject[GetEntryTwitterMetadata] = {
    forFeature("TWITTER_CARDS")(ifOn = BlogEntryTwitterMetadata.getTwitterCard, ifOff = BlogEntryTwitterMetadata.noTwitterCard)
  }

  implicit lazy val blogEntryFromYaml: Inject[blog.EntryFromYaml] = _ => EntryYaml.parse

  implicit lazy val blogEntryToYaml: Inject[blog.EntryToYaml] = _ => EntryYaml.format

  implicit lazy val blogEntriesToYaml: Inject[blog.EntriesToYaml] = _ => EntryYaml.formatAll

  implicit lazy val blogEntriesFromYaml: Inject[blog.EntriesFromYaml] = _ => EntryYaml.parseAll

  implicit val validateBlogIdentifier: Inject[duesoldi.blog.validation.ValidateIdentifier] = _ => ValidIdentifier.apply

  implicit val validateBlogContent: Inject[duesoldi.blog.validation.ValidateContent] = _ => ValidBlogContent.apply

  implicit val entryPageModel: Inject[BuildEntryPageModel] = inject(EntryPageModel.pageModel _)

  implicit val indexPageModel:  Inject[BuildIndexPageModel] = _ => IndexPageModel.pageModel

  implicit val getBlogEntry: Inject[GetBlogEntry] = { config =>
    BlogStore.getOne(jdbcPerformQuery[BlogEntry](BlogStore.toBlogEntry(parseMarkdown(config)))(config))
  }

  implicit val getAllBlogEntries: Inject[GetAllBlogEntries] = { config =>
    BlogStore.getAll(jdbcPerformQuery[BlogEntry](BlogStore.toBlogEntry(parseMarkdown(config)))(config))
  }

  implicit val putBlogEntry: Inject[PutBlogEntry] = inject(BlogStore.put _)

  implicit val deleteAllBlogEntries: Inject[DeleteAllBlogEntries] = inject(BlogStore.deleteAll _)

  implicit val deleteBlogEntry: Inject[DeleteBlogEntry] = inject(BlogStore.delete _)

  implicit def createOrUpdateBlogEntry(implicit executionContext: ExecutionContext): Inject[CreateOrUpdateBlogEntry] = inject(BlogStore.createOrUpdate _)

}
