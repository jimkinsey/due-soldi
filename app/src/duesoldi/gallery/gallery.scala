package duesoldi

import duesoldi.gallery.model.Artwork
import duesoldi.gallery.serialisation.ArtworkYaml

package object gallery {
  type ArtworkFromYaml = String => Either[ArtworkYaml.ParseFailure, Artwork]
}
