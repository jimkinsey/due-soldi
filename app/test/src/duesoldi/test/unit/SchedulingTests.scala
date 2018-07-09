package duesoldi.test.unit

import dearboy.EventBus
import duesoldi.scheduling.Scheduling
import duesoldi.scheduling.Scheduling.Event.{DidPerformTask, FailurePerformingTask, WillPerformTask}
import hammerspace.testing.CustomMatchers._
import utest._

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Try}

object SchedulingTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "Scheduling a task" - {
      "broadcasts an event that the task will be performed" - {
        val bus = new RecordingBus()
        Scheduling.schedule(bus)("Test", 100.millis, () => Future.successful({}))
        eventually {
          assert(bus.published.headOption isSomeWhere(_.isInstanceOf[WillPerformTask]))
        }
      }
      "executes the task" - {
        var executed: Boolean = false
        val bus = new RecordingBus()
        Scheduling.schedule(bus)("Test", 100.millis, () => Future.successful({ executed = true }))
        eventually {
          assert(executed)
        }
      }
      "broadcasts an event that the task has been performed" - {
        val bus = new RecordingBus()
        Scheduling.schedule(bus)("Test", 100.millis, () => Future.successful({}))
        eventually {
          assert(bus.published exists(_.isInstanceOf[DidPerformTask]))
        }
      }
      "broadcasts an event if an error occurred performing a task" - {
        val bus = new RecordingBus()
        val failure = new RuntimeException()
        Scheduling.schedule(bus)("Test", 100.millis, () => Future.failed(failure))
        eventually {
          assert(bus.published exists(_.isInstanceOf[FailurePerformingTask]))
        }
      }
      "executes the task periodically" - {
        val bus = new RecordingBus()
        Scheduling.schedule(bus)("Test", 100.millis, () => Future.successful({}))
        eventually {
          assert(bus.published.count(_.isInstanceOf[DidPerformTask]) > 1)
        }
      }
    }
  }

  def eventually(assertion: => Unit) {
    val start = System.currentTimeMillis()

    def doIt: Try[Unit] = {
      Try(assertion) match {
        case Failure(_) if System.currentTimeMillis() - start < 5000 =>  doIt
        case Failure(t) => throw t
        case res => res
      }
    }

    doIt
  }

  class RecordingBus() extends EventBus
  {
    override def publish(event: Any) { published append event }

    lazy val published: mutable.Buffer[Any] = mutable.Buffer()
  }
}
