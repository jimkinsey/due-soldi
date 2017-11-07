package duesoldi.controller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.furniture.CurrentFurniturePath

import scala.concurrent.ExecutionContext

object FurnitureRoutes
{
  def furnitureRoutes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector): Route =
    path("furniture" / LongNumber / Remaining) { case (version, path) =>
      inject.dependency[CurrentFurniturePath] into { currentVersion =>
        currentVersion(path) match {
          case Right((currentPath, file)) if currentPath == s"/furniture/$version/$path" =>
            respondWithHeaders(
              RawHeader("Cache-Control", "max-age=3600"),
              RawHeader("Expires", ZonedDateTime.now().plusHours(1).format(RFC_1123_DATE_TIME))
            ) {
              getFromFile(file)
            }
          case Right(_) =>
            complete { BadRequest -> "Invalid path" }
          case _ =>
            complete { NotFound -> "File not found" }
        }
      }
    }
}