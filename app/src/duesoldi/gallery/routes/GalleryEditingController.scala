package duesoldi.gallery.routes

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.app.sessions.Sessions.{GetSessionCookie, validSession}
import duesoldi.blog.model.BlogEntry
import duesoldi.blog.pages.BlogEditingPageModel
import duesoldi.blog.storage._
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.blog.{EntriesFromYaml, EntriesToYaml, EntryFromYaml, EntryToYaml}
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.gallery.{ArtworkFromYaml, ArtworkToYaml, ArtworksFromYaml, ArtworksToYaml}
import duesoldi.gallery.storage.{DeleteAllArtworks, DeleteArtwork, GetAllArtworks, GetArtwork, PutArtwork, PutArtworks}
import duesoldi.rendering.Render
import hammerspace.markdown
import hammerspace.markdown.MarkdownDocument
import ratatoskr.ResponseBuilding._
import sommelier.handling.Unpacking._
import sommelier.routing.Routing._
import sommelier.routing.{Controller, Result}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import scala.concurrent.ExecutionContext


class GalleryEditingController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  // TODO id non-editable (for now)
  // TODO POST result needs to have some data in it - make a reusable method for the whole thing?

//  GET("/admin/artwork/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
//    for {
//      getSessionCookie <- provided[GetSessionCookie]
//      sessionCookie <- getSessionCookie(context.request) rejectWith 500
//
//      getEntry <- provided[GetBlogEntry]
//      selectedEntryId <- query[String]("entry").optional.firstValue
//
//      emptyEntry = BlogEntry("", MarkdownDocument.empty)
//      entry <- if (selectedEntryId.isDefined) {
//        getEntry(selectedEntryId.get) defaultTo emptyEntry
//      } else {
//        Result(emptyEntry)
//      }
//
//      getBlogEntries <- provided[GetAllBlogEntries]
//      entries <- getBlogEntries() rejectWith { _ => 500 }
//      model = BlogEditingPageModel(
//        entries = entries.map(entry =>
//          BlogEditingPageModel.Entry(
//            entry.id,
//            entry.description.getOrElse(""),
//            entry.content.raw,
//            entry.lastModified.format(ISO_ZONED_DATE_TIME)
//          )
//        ),
//        entry = BlogEditingPageModel.Entry(
//          id = entry.id,
//          description = entry.description.getOrElse(""),
//          content = entry.content.raw,
//          date = entry.lastModified.format(ISO_ZONED_DATE_TIME)
//        )
//      )
//
//      render <- provided[Render]
//      html <- render("blog-editing", model) rejectWith { failure => 500(s"Failed to render $failure") }
//    } yield {
//      200(html) cookie sessionCookie ContentType "text/html; charset=UTF-8"
//    }
//  }

//  POST("/admin/blog/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
//    for {
//      getSessionCookie <- provided[GetSessionCookie]
//      sessionCookie <- getSessionCookie(context.request) rejectWith 500
//
//      parseMarkdown <- provided[markdown.Parse]
//      id <- form[String]("id").firstValue.required { 500 }
//      date <- form[ZonedDateTime]("date").optional.firstValue defaultTo ZonedDateTime.now()
//      description <- form[String]("description").optional.firstValue
//      content <- form[String]("content").firstValue.required { 500 }
//      entry = BlogEntry(id, content = parseMarkdown(content), description = description, lastModified = date)
//
//      store <- provided[CreateOrUpdateBlogEntry]
//      _ <- store(entry) rejectWith { _ => 500 }
//
//      getBlogEntries <- provided[GetAllBlogEntries]
//      entries <- getBlogEntries() rejectWith { _ => 500 }
//      model = BlogEditingPageModel(
//        entries = entries.map(entry =>
//          BlogEditingPageModel.Entry(
//            entry.id,
//            entry.description.getOrElse(""),
//            entry.content.raw,
//            entry.lastModified.format(ISO_ZONED_DATE_TIME)
//          )
//        ),
//        entry = BlogEditingPageModel.Entry(
//          id = entry.id,
//          description = entry.description.getOrElse(""),
//          content = entry.content.raw,
//          entry.lastModified.format(ISO_ZONED_DATE_TIME)
//        )
//      )
//
//      render <- provided[Render]
//      html <- render("blog-editing", model) rejectWith { failure => 500 }
//    } yield {
//      201(html) cookie sessionCookie ContentType "text/html; charset=UTF-8"
//    }
//  }

  PUT("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      parse              <- provided[ArtworkFromYaml]
      putArtwork         <- provided[PutArtwork]
      getArtwork         <- provided[GetArtwork]

      id      <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      content <- body[String]
      _       <- getArtwork(id).map(_.toLeft(false)) rejectWith { _ => 409 (s"Artwork with ID '$id' already exists")}
      entry   <- parse(content) rejectWith { failure => 400 (s"Failed to parse this content [$failure] [$content]")}
      _       <- putArtwork(entry) rejectWith { _ => 500 ("Failed to store artwork") }
    } yield {
      201 ("")
    }
  }

  DELETE("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      deleteArtwork      <- provided[DeleteArtwork]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      _  <- deleteArtwork(id) rejectWith { _ => 500 ("Failed to delete blog entry")}
    } yield {
      204
    }
  }

  GET("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      getArtwork <- provided[GetArtwork]
      format <- provided[ArtworkToYaml]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      artwork <- getArtwork(id) rejectWith { 404 }
    } yield {
      200 (format(artwork))
    }
  }

  GET("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getArtworks <- provided[GetAllArtworks]
      format <- provided[ArtworksToYaml]

      artworks <- getArtworks()
    } yield {
      200 (format(artworks))
    }
  }

  PUT("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      putArtworks <- provided[PutArtworks]
      parse <- provided[ArtworksFromYaml]

      content <- body[String]
      artworks <- parse(content) rejectWith { failure => 400 (s"Failed to parse document - $failure") }
      _ <- putArtworks(artworks) rejectWith { failure => 500 (s"Failed to store artworks - $failure") }
    } yield {
      201
    }
  }

  DELETE("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      deleteWorks <- provided[DeleteAllArtworks]

      _ <- deleteWorks() rejectWith { _ => 500 }
    } yield {
      204
    }
  }

}
