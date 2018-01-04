package duesoldi.logging

import duesoldi.blog.pages.IndexPageMaker
import duesoldi.blog.routes.BlogIndexController.Event.BlogIndexPageNotRendered
import duesoldi.events.Events
import duesoldi.metrics.storage.AccessRecordStorage

object EventLogging
{
  def enable(events: Events, logger: Logger) {

    events.respondTo {
      case BlogIndexPageNotRendered(IndexPageMaker.Failure.BlogStoreEmpty) =>
        logger.error("No blog entries were found to render the index page")
      case BlogIndexPageNotRendered(IndexPageMaker.Failure.RenderFailure(cause)) =>
        logger.error(s"The index page failed to render - $cause")
    }

    events.respondTo {
      case AccessRecordStorage.Event.RecordFailure(cause) =>
        logger.error(s"Failed to record access - ${cause.getMessage}")
    }

  }
}
