package duesoldi.blog.routes

import duesoldi.app.RequestDependencies._
import duesoldi.blog.pages.BuildEntryPageModel
import duesoldi.blog.storage.GetBlogEntry
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.rendering.Render
import sommelier.handling.Unpacking._
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext
import ratatoskr.ResponseBuilding._

class BlogEntryController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/blog/:id") ->- { implicit context =>
    for {
      validationFailure <- provided[ValidateIdentifier]
      getEntry <- provided[GetBlogEntry]
      pageModel <- provided[BuildEntryPageModel]
      rendered <- provided[Render]

      validId = (id: String) => validationFailure(id).isEmpty
      id <- pathParam[String]("id").validate(validId) { 400 }
      entry <- getEntry(id) rejectWith { 404 }
      model = pageModel(entry)
      html <- rendered("blog-entry", model) rejectWith { failure => println(s"FAILED TO RENDER $failure"); 500 }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}
