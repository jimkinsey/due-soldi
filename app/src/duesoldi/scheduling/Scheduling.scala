package duesoldi.scheduling

import java.time.ZonedDateTime
import java.util.{Timer, TimerTask}

import dearboy.EventBus
import duesoldi.scheduling.Scheduling.Event.{DidPerformTask, FailurePerformingTask, WillPerformTask}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Scheduling
{
  def schedule(eventBus: EventBus)
              (name: String, period: Duration, task: () => Future[Unit])
              (implicit executionContext: ExecutionContext): Unit = {
    val timer = new Timer(name, true)

    def start(): Unit = {
      val promise = Promise[Unit]()
      timer.schedule(
        new TimerTask {
          override def run() {
            executionContext.execute(
              () => {
                eventBus publish WillPerformTask(name, ZonedDateTime.now())
                promise.completeWith(task())
              }
            )
          }
        },
        period.toMillis
      )

      promise.future.onComplete {
        case Success(_) =>
          eventBus publish DidPerformTask(name, ZonedDateTime.now())
          start()
        case Failure(t) =>
          eventBus publish FailurePerformingTask(name, t)
          start()
      }
    }

    start()
  }

  sealed trait Event
  object Event
  {
    case class WillPerformTask(name: String, timestamp: ZonedDateTime) extends Event
    case class DidPerformTask(name: String, timestamp: ZonedDateTime) extends Event
    case class FailurePerformingTask(name: String, throwable: Throwable) extends Event
  }
}
