package duesoldi.logging

import duesoldi.controller.{BlogEntryRoutes, BlogIndexRoutes}
import duesoldi.controller.BlogIndexRoutes.Event.BlogIndexPageNotRendered
import duesoldi.events.Events
import duesoldi.page.IndexPageFailure

class Logging(events: Events) {

  events.respondTo[BlogIndexRoutes.Event] {
    case BlogIndexPageNotRendered(IndexPageFailure.BlogStoreEmpty) =>
      System.err.println("No blog entries were found to render the index page")
    case BlogIndexPageNotRendered(IndexPageFailure.RenderFailure(cause)) =>
      System.err.println(s"The index page failed to render - $cause")
  }

  events.respondTo[BlogEntryRoutes.Event] {
    case BlogEntryRoutes.Event.InvalidId(id) =>
      System.err.println(s"ID '$id' is invalid")
    case BlogEntryRoutes.Event.EntryNotFound(id) =>
      System.err.println(s"Blog entry '$id' not found")
    case BlogEntryRoutes.Event.RenderFailure(cause) =>
      System.err.println(s"Failed to render blog entry - $cause")
  }

}
