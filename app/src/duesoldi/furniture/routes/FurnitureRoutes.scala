package duesoldi.furniture.routes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import duesoldi.app.RequestDependencies._
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.furniture.{CurrentPathAndContent, FurnitureCacheConfig}
import sommelier.handling.Unpacking._
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext
import ratatoskr.ResponseBuilding._

class FurnitureController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/furniture/:version/*") ->- { implicit context =>
    for {
      currentPathAndContent <- provided[CurrentPathAndContent]
      version <- pathParam[Long]("version")
      providedPath <- remainingPath
      furniture <- currentPathAndContent(providedPath).rejectWith({ _ => 404 }).validate(_.path == s"/furniture/$version/$providedPath")({ 400 ("Invalid path") })
      cacheConfig <- provided[FurnitureCacheConfig]
      cacheDuration = cacheConfig.durationHours.getOrElse(1)
    } yield {
      200
        .header("Content-Type" -> "application/octet-stream")
        .header("Cache-Control" -> s"max-age=${cacheDuration * 3600}")
        .header("Expires" -> ZonedDateTime.now().plusHours(cacheDuration).format(RFC_1123_DATE_TIME))
        .content(furniture.content)
    }
  }
}
