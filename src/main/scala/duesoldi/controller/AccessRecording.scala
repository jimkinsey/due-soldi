package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.headers.{Referer, `User-Agent`}
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import duesoldi.config.Configured
import duesoldi.controller.AccessRecording.Event.{RecordFailure, RecordSuccess}
import duesoldi.events.Events
import duesoldi.storage.AccessRecordStore
import duesoldi.storage.AccessRecordStore.Access

import scala.concurrent.ExecutionContext
import scala.util.Failure

trait AccessRecording { self: Configured =>

  implicit def executionContext: ExecutionContext

  def accessRecordStore: AccessRecordStore
  def events: Events

  def recordAccess: Directive[Unit] =
    extractRequestContext.flatMap { ctx =>
      val startTime = System.currentTimeMillis()
      mapResponse { response =>
        if (config.accessRecordingEnabled) {
          val duration = System.currentTimeMillis() - startTime
          accessRecordStore.record(Access(
            time = ZonedDateTime.now(),
            path = ctx.request.uri.path.toString,
            referer = ctx.request.header[Referer].map(_.getUri().toString),
            userAgent = ctx.request.header[`User-Agent`].map(_.value()),
            duration = duration,
            clientIp = ctx.request.headers.find(_.name == "Cf-Connecting-Ip").map(_.value),
            country = ctx.request.headers.find(_.name == "Cf-Ipcountry").map(_.value),
            statusCode = response.status.intValue
          )).onComplete {
            case Failure(ex) =>
              events.emit(RecordFailure(ex))
            case _ =>
              events.emit(RecordSuccess)
          }
        }
        response
      }
    }
}

object AccessRecording {
  sealed trait Event extends duesoldi.events.Event
  object Event {
    case class RecordFailure(cause: Throwable) extends Event
    case object RecordSuccess extends Event
  }
}