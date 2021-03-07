package duesoldi.gallery

import duesoldi.gallery.model.Artwork

import scala.concurrent.Future

package object storage
{
  type GetArtwork = (String) => Future[Option[Artwork]]
//  type GetAllBlogEntries = () => Future[List[BlogEntry]]
  type PutArtwork = (Artwork) => Future[Either[GalleryStore.PutResult.Failure.type, GalleryStore.PutResult.Created.type]]
//  type PutBlogEntries = (Seq[BlogEntry]) => Future[Either[BlogStore.PutResult.Failure.type, BlogStore.PutResult.Created.type]]
  type DeleteArtwork = (String) => Future[Either[GalleryStore.DeleteResult.Failure.type, GalleryStore.DeleteResult.Deleted.type]]
//  type DeleteAllBlogEntries= () => Future[Either[BlogStore.DeleteResult.Failure.type, BlogStore.DeleteResult.Deleted.type]]
  type CreateOrUpdateArtwork = (Artwork) => Future[Either[GalleryStore.CreateOrUpdateResult.Failure.type, GalleryStore.CreateOrUpdateResult.Success]]
}
