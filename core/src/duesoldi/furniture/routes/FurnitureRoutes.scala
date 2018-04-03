package duesoldi.furniture.routes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

import duesoldi.app.RequestDependencies._
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.furniture.CurrentUrlPath
import sommelier.routing.Routing._
import sommelier.handling.Unpacking._
import sommelier.routing.Controller

import scala.concurrent.ExecutionContext

class FurnitureController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/furniture/:version/*") ->- { implicit context =>
    for {
      currentPathAndContent <- provided[CurrentUrlPath]
      version <- pathParam[Long]("version")
      providedPath <- remainingPath
      furniture <- currentPathAndContent(providedPath).rejectWith({ _ => 404 }).validate(_._1 == s"/furniture/$version/$providedPath")({ 400 ("Invalid path") })
      fileContent = furniture._2
      contentType = "application/octet-stream"
    } yield {
      200 (fileContent) ContentType contentType header("Cache-Control" -> "max-age=3600") header("Expires" -> ZonedDateTime.now().plusHours(1).format(RFC_1123_DATE_TIME))
    }
  }
}
