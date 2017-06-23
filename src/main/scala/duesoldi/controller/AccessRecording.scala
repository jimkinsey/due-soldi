package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.headers.{Referer, `User-Agent`}
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.storage.AccessRecordStore
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext
import scala.util.Failure

trait AccessRecording { self: Configured =>

  implicit def executionContext: ExecutionContext

  def accessRecordStore: AccessRecordStore

  def recordAccess =
    extractRequestContext.flatMap { ctx =>
      val startTime = System.currentTimeMillis()
      mapResponse { response =>
        if (config.accessRecordingEnabled) {
          val duration = System.currentTimeMillis() - startTime
          System.out.println(s"RECORDING ACCESS FOR REQUEST WITH HEADERS: ${ctx.request.headers.map({ h => s"${h.name}: ${h.value}" }).mkString("\n") }")
          accessRecordStore.record(Access(
            time = ZonedDateTime.now(),
            path = ctx.request.uri.path.toString,
            referer = ctx.request.header[Referer].map(_.getUri().toString),
            userAgent = ctx.request.header[`User-Agent`].map(_.value()),
            duration = duration,
            clientIp = ctx.request.headers.find(_.name == "CF-Connecting-IP").map(_.value),
            country = ctx.request.headers.find(_.name == "CF-IPCountry").map(_.value),
            statusCode = response.status.intValue
          )).onComplete {
            case Failure(ex) =>
              System.err.println(s"Failed to record access - ${ex.getMessage}")
              ex.printStackTrace()
            case _ =>
          }
        }
        response
      }
    }
}
