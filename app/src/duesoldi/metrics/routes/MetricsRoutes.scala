package duesoldi.metrics.routes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.GetAccessRecords
import sommelier.handling.Unpacking._
import sommelier.routing.Controller
import sommelier.routing.Routing._

import scala.concurrent.ExecutionContext

class MetricsController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{
  GET("/admin/metrics/access.csv").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getAccessRecords <- provided[GetAccessRecords]
      start <- query[ZonedDateTime]("start").optional.firstValue defaultTo sevenDaysAgo
      accesses <- getAccessRecords(start)
      content = renderCsv(accesses)
    } yield {
      200 (content) header("Cache-control" -> "no-cache")
    }
  }

  GET("/admin/metrics/access.json").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getAccessRecords <- provided[GetAccessRecords]
      start <- query[ZonedDateTime]("start").optional.firstValue defaultTo sevenDaysAgo
      accesses <- getAccessRecords(start)
      content = renderJson(accesses)
    } yield {
      200 (content) header("Cache-control" -> "no-cache") header("Content-type" -> "application/json")
    }
  }

  private def sevenDaysAgo = ZonedDateTime.now().minus(7, ChronoUnit.DAYS)

  private def renderCsv(accessRecords: Seq[Access]): String = {
    val rows = accessRecords.map { case Access(time, path, referer, userAgent, duration, ip, country, statusCode) =>
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

  private def csvFriendly(value: String): String =
    if (value.contains("\"") || value.contains(",")) {
      s""""${value.replace("\"", "\"\"").trim}""""
    }
    else {
      value.trim
    }

  private def renderJson(accessRecords: Seq[Access]): String = {

    def jsonSafe(in: String): String = in.replace("\"", """\"""")

    def optionalJsonField(name: String, value: Option[String]): String = {
      s""" "$name": ${value.map(defined => s""""${jsonSafe(defined)}"""").getOrElse("null")} """
    }

    s"""{
       |  "records": [
       |    ${accessRecords.map{ case Access(time, path, referer, userAgent, duration, ip, country, statusCode) =>
                s"""{
                   |  "time": "${time.format(DateTimeFormatter.ISO_DATE_TIME)}",
                   |  "path": "$path",
                   |  ${optionalJsonField("referer", referer)},
                   |  ${optionalJsonField("userAgent", userAgent)},
                   |  "duration": $duration,
                   |  ${optionalJsonField("ip", ip)},
                   |  ${optionalJsonField("country", country)},
                   |  "statusCode": $statusCode
                   |}""".stripMargin
             }.mkString(",")}
       |  ]
       |}""".stripMargin
  }
}
