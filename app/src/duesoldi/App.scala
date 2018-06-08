package duesoldi

import java.time.ZonedDateTime

import dearboy.EventBus
import duesoldi.app.{RequestId, TrailingSlashRedirection}
import duesoldi.blog.routes.{BlogEditingController, BlogEntryController, BlogIndexController}
import duesoldi.config.{Config, EnvironmentalConfig}
import duesoldi.controller.{LearnJapaneseController, RobotsController}
import duesoldi.debug.routes.DebugController
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.Injection.injected
import duesoldi.furniture.routes.FurnitureController
import duesoldi.logging.Logger
import duesoldi.metrics.routes.MetricsController
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.{AccessRecordStorage, StoreAccessRecord}
import hammerspace.collections.MapEnhancements._
import sommelier.events.Completed
import sommelier.serving.Server

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import ratatoskr.RequestAccess._

object App
{
  implicit val executionContext: ExecutionContext = concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    val env: Map[String, String] = System.getenv().asScala.toMap
    start(env)
  }

  def start(env: Env): Future[Server] = {
    implicit val config: Config = EnvironmentalConfig(env)
    val logger = new Logger("App", config.loggingEnabled)
    val events = new EventBus
    AccessRecordStorage.enable(events, injected[StoreAccessRecord])

    Future fromTry {
      Server.start(
        controllers = Seq(
          new BlogEntryController(),
          new BlogIndexController(),
          new BlogEditingController(),
          new FurnitureController(),
          RobotsController,
          LearnJapaneseController,
          new DebugController(),
          new MetricsController()
        ),
        middleware =
          TrailingSlashRedirection.middleware ++
          RequestId.middleware,
        port = config.port
      ) map {
        server =>
          logger.info(s"Started server on ${server.host}:${server.port}")
          server.subscribe {
            case Completed(req, res, duration) if config.accessRecordingEnabled => {
              val access = Access(
                time = ZonedDateTime.now(),
                path = req.path,
                referer = req.headers.lowKeys.get("referer").flatMap(_.headOption),
                userAgent = req.headers.lowKeys.get("user-agent").flatMap(_.headOption),
                duration = duration.toMillis,
                clientIp = req.headers.lowKeys.get("cf-connecting-ip").flatMap(_.headOption),
                country = req.headers.lowKeys.get("cf-ipcountry").flatMap(_.headOption),
                statusCode = res.status
              )
              events.publish(access)
            }
            case sommelier.events.ExceptionWhileRouting(req, exception) => {
              logger.error(s"Exception while handling ${req.method} ${req.path} - ${exception.getMessage}")
            }
          }
          server
      } recoverWith {
        case exception =>
          logger.error(s"Failed to start server on ${config.host}:${config.port} - ${exception.getMessage}")
          Failure(exception)
      }
    }
  }
}
