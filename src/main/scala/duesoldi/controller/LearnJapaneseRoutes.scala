package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object LearnJapaneseRoutes {
  val learnJapaneseRoutes: Route =
    path("learn-japanese" /) {
      getFromFile(s"src/main/resources/static/learn-japanese/index.html")
    } ~
    path("learn-japanese" / Remaining) { path =>
      getFromFile(s"src/main/resources/static/learn-japanese/$path")
    }
}
