package duesoldi.scheduling

import java.time.ZonedDateTime
import java.util.{Timer, TimerTask}

import dearboy.EventBus
import duesoldi.scheduling.Scheduling.Event.{DidPerformTask, FailurePerformingTask, WillPerformTask}
import duesoldi.scheduling.Scheduling.Task.{OneOff, Periodic}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Scheduling
{
  type Action = () => Future[Unit]

  sealed trait Task {
    def name: String
    def action: Action
  }
  object Task
  {
    case class Periodic(name: String, period: Duration, action: Action) extends Task
    case class OneOff(name: String, delay: Duration, action: Action) extends Task
  }

  def schedule(eventBus: EventBus)
              (task: Task)
              (implicit executionContext: ExecutionContext): Unit = {
    val timer = new Timer(task.name, true)

    def start(): Unit = {
      val promise = Promise[Unit]()
      timer.schedule(
        new TimerTask {
          override def run() {
            executionContext.execute(
              () => {
                eventBus publish WillPerformTask(task.name, ZonedDateTime.now())
                promise.completeWith(task.action())
              }
            )
          }
        },
        task match {
          case periodic: Periodic => periodic.period.toMillis
          case oneOff: OneOff => oneOff.delay.toMillis
        }
      )

      def rescheduleIfAppropriate(): Unit = task match {
        case _: Periodic => start()
        case _ =>
      }

      promise.future.onComplete {
        case Success(_) =>
          eventBus publish DidPerformTask(task.name, ZonedDateTime.now())
          rescheduleIfAppropriate()
        case Failure(t) =>
          eventBus publish FailurePerformingTask(task.name, t)
          rescheduleIfAppropriate()
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
