package duesoldi.gallery

import duesoldi.gallery.model.Artwork

import scala.concurrent.Future

package object storage
{
  type GetArtwork = (String) => Future[Option[Artwork]]
  type GetAllArtworks = () => Future[List[Artwork]]
  type PutArtwork = (Artwork) => Future[Either[GalleryStore.PutResult.Failure.type, GalleryStore.PutResult.Created.type]]
  type PutArtworks = (Seq[Artwork]) => Future[Either[GalleryStore.PutResult.Failure.type, GalleryStore.PutResult.Created.type]]
  type DeleteArtwork = (String) => Future[Either[GalleryStore.DeleteResult.Failure.type, GalleryStore.DeleteResult.Deleted.type]]
  type DeleteAllArtworks= () => Future[Either[GalleryStore.DeleteResult.Failure.type, GalleryStore.DeleteResult.Deleted.type]]
  type CreateOrUpdateArtwork = (Artwork) => Future[Either[GalleryStore.CreateOrUpdateResult.Failure.type, GalleryStore.CreateOrUpdateResult.Success]]
}
