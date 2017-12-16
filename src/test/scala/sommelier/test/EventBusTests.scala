package sommelier.test

import sommelier.events.EventBus
import utest._

import scala.collection.mutable

object EventBusTests
extends TestSuite
{
  val tests = this
  {
    "An event bus" - {
      "applies all matching subscribers to an event" - {
        val messages: mutable.Buffer[String] = mutable.Buffer()
        val bus = new EventBus()
        bus.subscribe({
          case 42 => messages append "bar"
        })
        bus.subscribe({
          case 21 => messages append "foo"
        })
        bus.subscribe({
          case 42 => messages append "baz"
        })
        bus.publish(42)
        assert(messages == Seq("bar", "baz"))
      }
    }
  }
}
