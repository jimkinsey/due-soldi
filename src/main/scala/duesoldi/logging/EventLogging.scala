package duesoldi.logging

import duesoldi.controller.BlogIndexRoutes.Event.BlogIndexPageNotRendered
import duesoldi.controller.{AccessRecordingDirective, BlogIndexRoutes}
import duesoldi.events.Events
import duesoldi.page.{EntryPageMaker, IndexPageFailure}

class EventLogging(events: Events, logger: Logger) {

  events.respondTo[BlogIndexRoutes.Event] {
    case BlogIndexPageNotRendered(IndexPageFailure.BlogStoreEmpty) =>
      logger.error("No blog entries were found to render the index page")
    case BlogIndexPageNotRendered(IndexPageFailure.RenderFailure(cause)) =>
      logger.error(s"The index page failed to render - $cause")
  }

  events.respondTo[EntryPageMaker.Event] {
    case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.InvalidId(id)) =>
      logger.error(s"ID '$id' is invalid")
    case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.EntryNotFound(id)) =>
      logger.error(s"Blog '$id' not found")
    case EntryPageMaker.Event.FailedToMakePage(EntryPageMaker.Failure.RenderFailure(cause)) =>
      logger.error(s"Failed to render blog entry - $cause")
  }

  events.respondTo[AccessRecordingDirective.Event] {
    case AccessRecordingDirective.Event.RecordFailure(cause) =>
      logger.error(s"Failed to record access - ${cause.getMessage}")
  }

}
