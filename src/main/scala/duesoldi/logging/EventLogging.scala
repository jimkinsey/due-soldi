package duesoldi.logging

import duesoldi.events.Events
import duesoldi.metrics.storage.AccessRecordStorage

object EventLogging
{
  def enable(events: Events, logger: Logger) {
    events.respondTo {
      case AccessRecordStorage.Event.RecordFailure(cause) =>
        logger.error(s"Failed to record access - ${cause.getMessage}")
    }
  }
}
