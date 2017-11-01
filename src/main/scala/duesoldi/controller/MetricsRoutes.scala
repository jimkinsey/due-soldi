package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.storage.AccessRecordStore
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext

object MetricsRoutes
{
  def metricsRoutes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector, config: Config): Route =
    path("admin" / "metrics" / "access.csv") {
      adminsOnly(config.adminCredentials) {
        inject.dependency[AccessRecordStore] into { accessRecordStore =>
          complete {
            accessRecordStore.allRecords.map(
              { accesses =>
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
                HttpResponse(entity = ("Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code" +: rows).mkString("\n"))
              }).recover {
              case ex =>
                ex.printStackTrace()
                HttpResponse(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }

  private def csvFriendly(value: String): String = s"""$value""" // TODO quote value or escape commas

}
