package duesoldi.app

import sommelier.routing.Controller

object TrailingSlashRedirection
extends Controller
{
  import sommelier.routing.Routing._

  AnyRequest >-- { request =>
    request.path match {
      case "/learn-japanese" => rejectRequest(301 Location "/learn-japanese/")
      case "/blog" => rejectRequest(301 Location "/blog/")
      case _ => request
    }
  }
}
