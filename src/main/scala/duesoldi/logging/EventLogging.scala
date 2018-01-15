package duesoldi.logging

import dearboy.EventBus
import duesoldi.metrics.storage.AccessRecordStorage

object EventLogging
{
  def enable(events: EventBus, logger: Logger) {
    events.subscribe {
      case AccessRecordStorage.Event.RecordFailure(cause) =>
        logger.error(s"Failed to record access - ${cause.getMessage}")
    }
  }
}
