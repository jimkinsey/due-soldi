package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.storage.AccessRecordStore
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext

trait MetricsRoutes extends AdminAuthentication { self: Configured =>
  implicit def executionContext: ExecutionContext

  def accessRecordStore: AccessRecordStore

  final def metricsRoutes =
    path("admin" / "metrics" / "access.csv") {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        complete {
          accessRecordStore.allRecords.map(
            { accesses =>
              val rows = accesses.map { case Access(time, path, referer, userAgent, duration) =>
                Seq(
                  time.format(DateTimeFormatter.ISO_DATE_TIME),
                  path,
                  referer.getOrElse(""),
                  userAgent.getOrElse(""),
                  duration.toString
                ).map(csvFriendly).mkString(",")
              }
              HttpResponse(entity = ("Timestamp,Path,Referer,User-Agent,Duration (ms)" +: rows).mkString("\n")) // Duration,Status code,IP
            }).recover { case ex =>
              ex.printStackTrace()
              HttpResponse(StatusCodes.InternalServerError)
            }
          }
        }
      }

  private def csvFriendly(value: String): String = s"""$value""" // TODO quote value or escape commas

}
