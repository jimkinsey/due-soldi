package duesoldi.blog.routes

import duesoldi.blog.pages.{IndexPageMaker, MakeIndexPage}
import duesoldi.blog.routes.BlogIndexController.Event.{BlogIndexPageNotRendered, BlogIndexPageRendered}
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.events.Emit
import duesoldi.app.TempSommelierIntegration._
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext

class BlogIndexController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/blog/") ->- { implicit context =>
    for {
      makePage <- provided[MakeIndexPage]
      emit <- provided[Emit]
      html <- makePage() rejectWith {
        case failure: IndexPageMaker.Failure.BlogStoreEmpty.type =>
          emit(BlogIndexPageNotRendered(failure))
          404
        case failure =>
          emit(BlogIndexPageNotRendered(failure))
          500
      }
    } yield {
      emit(BlogIndexPageRendered(html))
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}

object BlogIndexController
{
  sealed trait Event
  object Event
  {
    case class BlogIndexPageRendered(html: String) extends Event
    case class BlogIndexPageNotRendered(reason: IndexPageMaker.Failure) extends Event
  }
}

