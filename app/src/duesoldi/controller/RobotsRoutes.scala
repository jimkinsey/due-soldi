package duesoldi.controller

import ratatoskr.ResponseBuilding._
import sommelier.routing.Controller
import sommelier.routing.Routing._

object RobotsController
extends Controller
{
  GET("/robots.txt") ->- { _ =>
    200 ("User-agent: *\nDisallow:\n") header "Cache-Control" -> "max-age=86400"
  }
}
