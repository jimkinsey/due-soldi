package duesoldi.logging

import dearboy.EventBus
import duesoldi.metrics.storage.{AccessRecordArchiveStorage, AccessRecordStorage}

object EventLogging
{
  def enable(events: EventBus, logger: Logger) {
    events.subscribe {
      case AccessRecordStorage.Event.RecordFailure(cause) =>
        logger.error(s"Failed to record access - ${cause.getMessage}")
      case AccessRecordArchiveStorage.Event.ArchiveSuccess(count) =>
        logger.info(s"Archived $count access records")
      case AccessRecordArchiveStorage.Event.ArchiveFailure(cause) =>
        logger.error(s"Failed to archive access records - ${cause.getMessage}")
    }
  }
}
