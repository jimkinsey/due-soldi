package duesoldi.logging

import dearboy.EventBus
import duesoldi.App.{ServerStartFailure, ServerStarted}
import duesoldi.metrics.storage.{AccessRecordArchiveStorage, AccessRecordStorage}
import duesoldi.scheduling.Scheduling

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
      case ServerStarted(host, port) =>
        logger.info(s"Started server on $host:$port")
      case ServerStartFailure(host, port, cause) =>
        logger.error(s"Failed to start server on $host:${port.getOrElse("-")} - ${cause.getMessage}")
      case Scheduling.Event.WillPerformTask(name, _) =>
        logger.info(s"Performing scheduled task '$name'...")
      case Scheduling.Event.DidPerformTask(name, _) =>
        logger.info(s"Finished performing scheduled task '$name'")
      case Scheduling.Event.FailurePerformingTask(name, cause) =>
        logger.error(s"Error while performing scheduled task '$name', ${cause.getMessage}")
    }
  }
}
