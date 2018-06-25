package duesoldi.metrics.routes

import java.io.File
import java.nio.file.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.app.sessions.Sessions.{GetSessionCookie, validSession}
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.GetAccessRecords
import ratatoskr.ResponseBuilding._
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

  OPTIONS("/admin/metrics/access.json") ->- { implicit context =>
    200
      .header("Access-Control-Allow-Origin" -> "*")
      .header("Access-Control-Allow-Methods" -> "GET")
      .header("Access-Control-Allow-Headers" -> "authorization, cookie")
  }

  GET("/admin/metrics/access.json").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500

      getAccessRecords <- provided[GetAccessRecords]
      start <- query[ZonedDateTime]("start").optional.firstValue defaultTo sevenDaysAgo
      accesses <- getAccessRecords(start)
      content = renderJson(accesses)
    } yield {
      200 (content)
        .header("Access-Control-Allow-Origin" -> "*")
        .header("Cache-control" -> "no-cache")
        .header("Content-type" -> "application/json")
        .cookie(sessionCookie)
    }
  }

  GET("/admin/metrics/access/").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500
    } yield {
      200 (fileContent("app/resources/static/access-overview/")("index.html"))
        .header("Cache-control" -> "no-cache")
        .header("Content-type" -> "text/html; charset=UTF-8")
        .cookie(sessionCookie)
    }
  }

  private def fileContent(basePath: String)(path: String): Array[Byte] = {
    Files.readAllBytes(new File(basePath + path).toPath)
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
                   |  "time": "${time.format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss"))}",
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
