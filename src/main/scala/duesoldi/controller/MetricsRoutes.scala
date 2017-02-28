package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import duesoldi.config.Configured
import duesoldi.storage.AccessRecordStore
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext

trait MetricsRoutes { self: Configured =>
  implicit def executionContext: ExecutionContext

  def accessRecordStore: AccessRecordStore

  final def metricsRoutes =
    path("admin" / "metrics" / "access.csv") {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        complete {
          accessRecordStore.allRecords.map(
            { accesses =>
              val rows = accesses.map { case Access(time, path, referer) =>
                Seq(
                  time.format(DateTimeFormatter.ISO_DATE_TIME),
                  path,
                  referer.getOrElse("")
                ).map(csvFriendly).mkString(",")
              }
              HttpResponse(entity = ("Timestamp,Path,Referer" +: rows).mkString("\n")) // ,User-agent,Duration,Status code,IP
            }).recover { case ex =>
              ex.printStackTrace()
              HttpResponse(StatusCodes.InternalServerError)
            }
          }
        }
      }


  def authenticatedAdminUser: Authenticator[String] = {
    case password @ Credentials.Provided(username) if config.adminCredentials.exists(creds => password.verify(creds.password)) => Some(username)
    case _ => None
  }

  private def csvFriendly(value: String): String = s"""$value""" // TODO quote value or escape commas

}
