package duesoldi

import duesoldi.gallery.model.{Artwork, Series}
import duesoldi.gallery.serialisation.{ArtworkYaml, SeriesYaml}

package object gallery {
  type ArtworkFromYaml = String => Either[ArtworkYaml.ParseFailure, Artwork]
  type ArtworksFromYaml = String => Either[ArtworkYaml.ParseFailure, Seq[Artwork]]
  type ArtworksToYaml = Seq[Artwork] => String
  type ArtworkToYaml = Artwork => String

  type ManySeriesFromYaml = String => Either[SeriesYaml.ParseFailure, Seq[Series]]
  type SeriesFromYaml = String => Either[SeriesYaml.ParseFailure, Series]
}

object Thumbnails {

  type GetThumbnailURL = (String) => (String)

  def getURL(): GetThumbnailURL = { imageURL =>
    imageURL.take(imageURL.lastIndexOf('.')) + "-w200.jpg" // FIXME extension
  }

}