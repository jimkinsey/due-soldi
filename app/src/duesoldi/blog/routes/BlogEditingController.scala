package duesoldi.blog.routes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.blog.storage._
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.blog.{EntriesFromYaml, EntriesToYaml, EntryFromYaml, EntryToYaml}
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import sommelier.handling.Unpacking._
import sommelier.routing.{Controller, Result, SyncResult}
import sommelier.routing.Routing._
import ratatoskr.ResponseBuilding._

import scala.concurrent.{ExecutionContext, Future}
import duesoldi.app.sessions.Sessions.{GetSessionCookie, validSession}
import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages.BlogEditingPageModel
import duesoldi.rendering.Render
import hammerspace.markdown
import hammerspace.markdown.{MarkdownDocument, MarkdownParser}


class BlogEditingController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  // TODO id non-editable (for now)
  // TODO POST result needs to have some data in it - make a reusable method for the whole thing?

  GET("/admin/blog/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500

      getEntry <- provided[GetBlogEntry]
      selectedEntryId <- query[String]("entry").optional.firstValue

      emptyEntry = BlogEntry("", MarkdownDocument.empty)
      entry <- if (selectedEntryId.isDefined) {
        getEntry(selectedEntryId.get) defaultTo emptyEntry
      } else {
        Result(emptyEntry)
      }

      getBlogEntries <- provided[GetAllBlogEntries]
      entries <- getBlogEntries() rejectWith { _ => 500 }
      model = BlogEditingPageModel(
        entries = entries.map(entry =>
          BlogEditingPageModel.Entry(
            entry.id,
            entry.description.getOrElse(""),
            entry.content.raw,
            entry.lastModified.format(ISO_ZONED_DATE_TIME)
          )
        ),
        entry = BlogEditingPageModel.Entry(
          id = entry.id,
          description = entry.description.getOrElse(""),
          content = entry.content.raw,
          date = entry.lastModified.format(ISO_ZONED_DATE_TIME)
        )
      )

      render <- provided[Render]
      html <- render("blog-editing", model) rejectWith { failure => 500(s"Failed to render $failure") }
    } yield {
      200(html) cookie sessionCookie
    }
  }

  POST("/admin/blog/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500

      parseMarkdown <- provided[markdown.Parse]
      id <- form[String]("id").firstValue.required { 500 }
      date <- form[ZonedDateTime]("date").optional.firstValue defaultTo ZonedDateTime.now()
      description <- form[String]("description").optional.firstValue
      content <- form[String]("content").firstValue.required { 500 }
      entry = BlogEntry(id, content = parseMarkdown(content), description = description, lastModified = date)

      store <- provided[CreateOrUpdateBlogEntry]
      _ <- store(entry) rejectWith { _ => 500 }

      getBlogEntries <- provided[GetAllBlogEntries]
      entries <- getBlogEntries() rejectWith { _ => 500 }
      model = BlogEditingPageModel(
        entries = entries.map(entry =>
          BlogEditingPageModel.Entry(
            entry.id,
            entry.description.getOrElse(""),
            entry.content.raw,
            entry.lastModified.format(ISO_ZONED_DATE_TIME)
          )
        ),
        entry = BlogEditingPageModel.Entry(
          id = entry.id,
          description = entry.description.getOrElse(""),
          content = entry.content.raw,
          entry.lastModified.format(ISO_ZONED_DATE_TIME)
        )
      )

      render <- provided[Render]
      html <- render("blog-editing", model) rejectWith { failure => 500 }
    } yield {
      201(html) cookie sessionCookie
    }
  }

  PUT("/admin/blog/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      parse <- provided[EntryFromYaml]
      putEntry <- provided[PutBlogEntry]
      getEntry <- provided[GetBlogEntry]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      content <- body[String]
      _ <- getEntry(id).map(_.toLeft(false)) rejectWith { _ => 409 (s"Entry with ID '$id' already exists")}
      entry <- parse(content) rejectWith { failure => 400 (s"Failed to parse this content [$failure] [$content]")}
      _ <- putEntry(entry) rejectWith { _ => 500 ("Failed to store entry") }
    } yield {
      201 ("")
    }
  }

  DELETE("/admin/blog/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      deleteEntry <- provided[DeleteBlogEntry]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      _ <- deleteEntry(id) rejectWith { _ => 500 ("Failed to delete blog entry")}
    } yield {
      204
    }
  }

  GET("/admin/blog/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      getEntry <- provided[GetBlogEntry]
      format <- provided[EntryToYaml]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      entry <- getEntry(id) rejectWith { 404 }
    } yield {
      200 (format(entry))
    }
  }

  GET("/admin/blog").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getEntries <- provided[GetAllBlogEntries]
      format <- provided[EntriesToYaml]

      entries <- getEntries()
    } yield {
      200 (format(entries))
    }
  }

  PUT("/admin/blog").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      putEntries <- provided[PutBlogEntries]
      parse <- provided[EntriesFromYaml]

      content <- body[String]
      entries <- parse(content) rejectWith { failure => 400 (s"Failed to parse document - $failure") }
      _ <- putEntries(entries) rejectWith { failure => 500 (s"Failed to store entries - $failure") }
    } yield {
      201
    }
  }

  DELETE("/admin/blog").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      deleteEntries <- provided[DeleteAllBlogEntries]

      _ <- deleteEntries() rejectWith { _ => 500 }
    } yield {
      204
    }
  }

}
