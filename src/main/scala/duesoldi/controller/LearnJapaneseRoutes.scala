package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object LearnJapaneseRoutes {
  val learnJapaneseRoutes: Route = path("learn-japanese") { getFromFile("src/main/resources/static/learn-japanese.html") }
}
