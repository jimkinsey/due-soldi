package duesoldi.blog.routes

import duesoldi.app.RequestDependencies._
import duesoldi.blog.pages.BuildIndexPageModel
import duesoldi.blog.storage.GetAllBlogEntries
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.rendering.Render
import sommelier.handling.Unpacking._
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext

class BlogIndexController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/blog/") ->- { implicit context =>
    for {
      getEntries <- provided[GetAllBlogEntries]
      rendered <- provided[Render]
      pageModel <- provided[BuildIndexPageModel]

      entries <- getEntries().rejectWith({ _ => 500 }).validate(_.nonEmpty)({ 404 })
      model = pageModel(entries)
      html <- rendered("blog-index", model) rejectWith { _ => 500 }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}
