package duesoldi.app

import java.util.UUID

import ratatoskr.RequestBuilding._
import ratatoskr.ResponseBuilding._
import sommelier.routing.Controller

object RequestId
extends Controller
{
  import sommelier.routing.Routing._

  // todo add func test for this
  AnyRequest >-- { _ header "Request-ID" -> UUID.randomUUID().toString }
  AnyRequest --> { (req, res) => res header "Request-ID" -> req.headers("Request-ID").head }
}
