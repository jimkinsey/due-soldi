package duesoldi.blog.routes

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.blog.storage._
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.blog.{EntriesFromYaml, EntriesToYaml, EntryFromYaml, EntryToYaml}
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import sommelier.handling.Unpacking._
import sommelier.routing.Controller
import sommelier.routing.Routing._
import ratatoskr.ResponseBuilding._

import scala.concurrent.ExecutionContext
import duesoldi.app.sessions.Sessions.{GetSessionCookie, validSession}


class BlogEditingController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  GET("/admin/blog/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500
    } yield {
      200 cookie sessionCookie
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
