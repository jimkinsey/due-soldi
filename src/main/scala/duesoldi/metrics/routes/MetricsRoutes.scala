package duesoldi.metrics.routes

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.GetAllAccessRecords

import scala.concurrent.ExecutionContext

object MetricsRoutes
{
  def metricsRoutes(implicit executionContext: ExecutionContext, inject: RequestDependencyInjector): Route =
    path("admin" / "metrics" / "access.csv") {
      inject.dependencies[GetAllAccessRecords, Credentials] into { case (getAccessRecords, adminCredentials) =>
        adminsOnly(adminCredentials) {
          complete {
            getAccessRecords().map(
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
