package duesoldi.blog

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.storage.BlogStore

import scala.concurrent.Future

package object storage
{
  type GetBlogEntry = (String) => Future[Option[BlogEntry]]
  type GetAllBlogEntries = () => Future[List[BlogEntry]]
  type PutBlogEntry = (BlogEntry) => Future[Either[BlogStore.PutResult.Failure.type, BlogStore.PutResult.Created.type]]
  type DeleteBlogEntry = (String) => Future[Unit]
}
