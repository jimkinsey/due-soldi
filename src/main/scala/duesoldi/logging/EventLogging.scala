package duesoldi.logging

import duesoldi.blog.pages.{EntryPageMaker, IndexPageMaker}
import duesoldi.blog.routes.BlogIndexRoutes.Event.BlogIndexPageNotRendered
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
      case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.InvalidId(id)) =>
        logger.error(s"ID '$id' is invalid")
      case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.EntryNotFound(id)) =>
        logger.error(s"Blog '$id' not found")
      case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.RenderFailure(cause)) =>
        logger.error(s"Failed to render blog entry - $cause")
    }

    events.respondTo {
      case AccessRecordStorage.Event.RecordFailure(cause) =>
        logger.error(s"Failed to record access - ${cause.getMessage}")
    }

  }
}
