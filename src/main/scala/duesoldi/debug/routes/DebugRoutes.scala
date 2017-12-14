package duesoldi.debug.routes

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.TempSommelierIntegration._
import duesoldi.config.Config
import duesoldi.debug.pages.{MakeConfigPage, MakeHeadersPage}
import duesoldi.dependencies.DueSoldiDependencies._
import sommelier.Controller
import sommelier.Routing._

import scala.concurrent.ExecutionContext

class DebugController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/admin/debug/headers").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      makePage <- provided[MakeHeadersPage]
      page = makePage(context.request)
    } yield {
      200 (page) ContentType "text/plain"
    }
  }

  GET("/admin/debug/config").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      makePage <- provided[MakeConfigPage]
      page = makePage()
    } yield {
      200 (page) ContentType "text/plain"
    }
  }
}
