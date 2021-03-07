package duesoldi

import duesoldi.gallery.model.Artwork
import duesoldi.gallery.serialisation.ArtworkYaml

package object gallery {
  type ArtworkFromYaml = String => Either[ArtworkYaml.ParseFailure, Artwork]
  type ArtworksFromYaml = String => Either[ArtworkYaml.ParseFailure, Seq[Artwork]]
  type ArtworksToYaml = Seq[Artwork] => String
  type ArtworkToYaml = Artwork => String
}
