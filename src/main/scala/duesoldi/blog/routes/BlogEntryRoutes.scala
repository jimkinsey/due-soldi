package duesoldi.blog.routes

import duesoldi.app.TempSommelierIntegration._
import duesoldi.blog.pages.EntryPageMaker.Failure
import duesoldi.blog.pages.MakeEntryPage
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import sommelier.Controller
import sommelier.Routing._
import sommelier.Unpacking._

import scala.concurrent.ExecutionContext

class BlogEntryController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/blog/:id") ->- { implicit context =>
    for {
      id <- pathParam[String]("id") // todo validation here
      entryPage <- provided[MakeEntryPage]
      html <- entryPage(id) rejectWith {
        case Failure.EntryNotFound(_) => 404
        case Failure.InvalidId(_) => 400
        case _ => 500
      }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}
