package duesoldi.storage

import duesoldi.model.BlogEntry

import scala.concurrent.Future

package object blog {
  type Entry = (String) => Future[Option[BlogEntry]]
}
