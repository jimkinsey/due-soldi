package duesoldi.blog

import duesoldi.blog.model.BlogEntry

import scala.concurrent.Future

package object storage
{
  type GetBlogEntry = (String) => Future[Option[BlogEntry]]
  type GetAllBlogEntries = () => Future[List[BlogEntry]]
  type PutBlogEntry = (BlogEntry) => Future[Either[BlogStore.PutResult.Failure.type, BlogStore.PutResult.Created.type]]
  type PutBlogEntries = (Seq[BlogEntry]) => Future[Either[BlogStore.PutResult.Failure.type, BlogStore.PutResult.Created.type]]
  type DeleteBlogEntry = (String) => Future[Either[BlogStore.DeleteResult.Failure.type, BlogStore.DeleteResult.Deleted.type]]
  type DeleteAllBlogEntries= () => Future[Either[BlogStore.DeleteResult.Failure.type, BlogStore.DeleteResult.Deleted.type]]
}
