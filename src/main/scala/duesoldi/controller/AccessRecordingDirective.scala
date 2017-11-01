package duesoldi.controller

import java.time.ZonedDateTime

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.{Referer, `User-Agent`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, RequestContext}
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.events.Emit
import duesoldi.storage.AccessRecordStore.Access

object AccessRecordingDirective
{
  def recordAccess(implicit inject: RequestDependencyInjector): Directive[Unit] =
    extractRequestContext.flatMap { implicit ctx =>
      import ctx.executionContext
      inject.dependency[Emit].into.flatMap { emit =>
        val startTime = System.currentTimeMillis()
        mapResponse { response =>
          val duration = System.currentTimeMillis() - startTime
          emit(access(ctx, response, duration))
          response
        }
      }
    }

  private def access(ctx: RequestContext, response: HttpResponse, duration: Long) = Access(
    time = ZonedDateTime.now(),
    path = ctx.request.uri.path.toString,
    referer = ctx.request.header[Referer].map(_.getUri().toString),
    userAgent = ctx.request.header[`User-Agent`].map(_.value()),
    duration = duration,
    clientIp = ctx.request.headers.find(_.name == "Cf-Connecting-Ip").map(_.value),
    country = ctx.request.headers.find(_.name == "Cf-Ipcountry").map(_.value),
    statusCode = response.status.intValue()
  )
}