package duesoldi.controller

import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route

object LearnJapaneseRoutes {
  val learnJapaneseRoutes: Route =
    pathPrefix("learn-japanese") {
      pathEndOrSingleSlash {
        redirectToTrailingSlashIfMissing(MovedPermanently) {
          getFromFile(s"src/main/resources/static/learn-japanese/index.html")
        }
      } ~
      path(Remaining) { path =>
        getFromFile(s"src/main/resources/static/learn-japanese/$path")
      }
    }
}
