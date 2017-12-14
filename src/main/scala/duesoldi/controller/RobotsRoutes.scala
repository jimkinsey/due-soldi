package duesoldi.controller

import sommelier.Controller
import sommelier.Routing._

object RobotsController
extends Controller
{
  GET("/robots.txt") ->- { _ =>
    200 ("User-agent: *\nDisallow:\n") header "Cache-Control" -> "max-age=86400"
  }
}
