package duesoldi.controller

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.HttpResponse
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
          for {
            accesses <- accessRecordStore.allRecords
          } yield {
            val rows = accesses.map { case Access(time, path) =>
              Seq(time.format(DateTimeFormatter.ISO_DATE_TIME), path).map(csvFriendly).mkString(",")
            }
            HttpResponse(entity = ("Timestamp,Path" +: rows).mkString("\n")) // ,Referer,User-agent,Duration,Status code,IP
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
