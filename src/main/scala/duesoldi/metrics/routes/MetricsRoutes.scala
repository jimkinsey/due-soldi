package duesoldi.metrics.routes

import java.time.format.DateTimeFormatter

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.TempSommelierIntegration._
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.GetAllAccessRecords
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext

class MetricsController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/admin/metrics/access.csv").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getAccessRecords <- provided[GetAllAccessRecords]
      accesses <- getAccessRecords()
      content = {
        val rows = accesses.map { case Access(time, path, referer, userAgent, duration, ip, country, statusCode) =>
          Seq(
            time.format(DateTimeFormatter.ISO_DATE_TIME),
            path,
            referer.getOrElse(""),
            userAgent.getOrElse(""),
            duration.toString,
            ip.getOrElse(""),
            country.getOrElse(""),
            statusCode.toString
          ).map(csvFriendly).mkString(",")
        }
        ("Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code" +: rows).mkString("\n")
      }
    } yield {
      200 (content) header("Cache-control" -> "no-cache")
    }
  }

  private def csvFriendly(value: String): String = s"""$value""" // TODO quote value or escape commas
}
