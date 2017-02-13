package duesoldi.controller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured

trait FurnitureRoutes { self: Configured =>

  final def furnitureRoutes = path("furniture" / Segment / Remaining) {
    case (version: String, remaining: String) if version == config.furnitureVersion =>
      val maxAge = config.furnitureCacheDuration.toSeconds
      respondWithHeaders(
        RawHeader("Cache-Control", s"max-age=$maxAge"),
        RawHeader("Expires", ZonedDateTime.now().plusSeconds(maxAge).format(DateTimeFormatter.RFC_1123_DATE_TIME))
      ) {
        getFromFile(config.furniturePath + "/" + remaining)
      }
    case _ => complete { HttpResponse(BadRequest) }
  }

}
