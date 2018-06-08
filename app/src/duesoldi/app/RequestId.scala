package duesoldi.app

import java.util.UUID

import sommelier.routing.Controller

import ratatoskr.RequestBuilding._

object RequestId
extends Controller
{
  import sommelier.routing.Routing._

  // todo add func test for this
  AnyRequest >-- { _ header "Request-ID" -> UUID.randomUUID().toString }
  AnyRequest --> { (req, res) => res header "Request-ID" -> req.headers("Request-ID").head }
}
