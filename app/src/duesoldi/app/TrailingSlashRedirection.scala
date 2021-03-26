package duesoldi.app

import sommelier.routing.Controller
import ratatoskr.RequestAccess._
import ratatoskr.ResponseBuilding._

object TrailingSlashRedirection
extends Controller
{
  import sommelier.routing.Routing._

  AnyRequest >-- { request =>
    request.path match {
      case "/learn-japanese" => rejectRequest(301 Location "/learn-japanese/")
      case "/blog" => rejectRequest(301 Location "/blog/")
      case "/gallery" => rejectRequest(301 Location "/gallery/")
      case "/series" => rejectRequest(301 Location "/gallery/")
      case "/series/" => rejectRequest(301 Location "/gallery/")
      case _ => request
    }
  }
}
