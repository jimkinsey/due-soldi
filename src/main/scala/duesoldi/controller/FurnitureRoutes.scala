package duesoldi.controller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config

import scala.concurrent.ExecutionContext

object FurnitureRoutes {
  def furnitureRoutes(config: Config)(implicit executionContext: ExecutionContext): Route =
    handleRejections(fileFailure) {
      path("furniture" / Segment / Remaining) {
        case (version: String, remaining: String) if version == config.furnitureVersion => {
          val maxAge = config.furnitureCacheDuration.toSeconds
          respondWithHeaders(
            RawHeader("Cache-Control", s"max-age=$maxAge"),
            RawHeader("Expires", ZonedDateTime.now().plusSeconds(maxAge).format(DateTimeFormatter.RFC_1123_DATE_TIME))
          ) {
            getFromFile(config.furniturePath + "/" + remaining)
          }
        }
        case _ => complete {
          HttpResponse(BadRequest)
        }
      }
    }

  private lazy val fileFailure = RejectionHandler.newBuilder
    .handleNotFound( complete(HttpResponse(NotFound)) )
    .result()
}
