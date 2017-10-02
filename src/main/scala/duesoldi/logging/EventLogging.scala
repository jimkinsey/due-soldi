package duesoldi.logging

import duesoldi.controller.{BlogEntryRoutes, BlogIndexRoutes}
import duesoldi.controller.BlogIndexRoutes.Event.BlogIndexPageNotRendered
import duesoldi.events.Events
import duesoldi.page.IndexPageFailure

class EventLogging(events: Events, logger: Logger) {

  events.respondTo[BlogIndexRoutes.Event] {
    case BlogIndexPageNotRendered(IndexPageFailure.BlogStoreEmpty) =>
      logger.error("No blog entries were found to render the index page")
    case BlogIndexPageNotRendered(IndexPageFailure.RenderFailure(cause)) =>
      logger.error(s"The index page failed to render - $cause")
  }

  events.respondTo[BlogEntryRoutes.Event] {
    case BlogEntryRoutes.Event.InvalidId(id) =>
      logger.error(s"ID '$id' is invalid")
    case BlogEntryRoutes.Event.EntryNotFound(id) =>
      logger.error(s"Blog entry '$id' not found")
    case BlogEntryRoutes.Event.RenderFailure(cause) =>
      logger.error(s"Failed to render blog entry - $cause")
  }

}
