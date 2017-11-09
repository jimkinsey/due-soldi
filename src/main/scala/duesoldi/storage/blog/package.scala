package duesoldi.storage

import duesoldi.model.BlogEntry

import scala.concurrent.Future

package object blog
{
  type GetBlogEntry = (String) => Future[Option[BlogEntry]]
  type GetAllBlogEntries = () => Future[List[BlogEntry]]
  type PutBlogEntry = (BlogEntry) => Future[Either[BlogStore.PutResult.Failure.type, BlogStore.PutResult.Created.type]]
  type DeleteBlogEntry = (String) => Future[Unit]
}
