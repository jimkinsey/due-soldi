package duesoldi.gallery

import duesoldi.blog.pages.{GetEntryTwitterMetadata, PageModel}
import duesoldi.gallery.model.Artwork
import duesoldi.gallery.pages.BuildArtworkPageModel
import hammerspace.markdown.MarkdownToHtml

package object pages {
  type BuildArtworkPageModel = (Artwork) => ArtworkPageModel
}

case class ArtworkPageModel(
  title: String,
  timeframe: Option[String],
  materials: Option[String],
  description: Option[String],
  imageURL: String
) extends PageModel

object ArtworkPageModel {
  def build(getEntryTwitterMetadata: GetEntryTwitterMetadata)(artwork: Artwork): ArtworkPageModel = {
    ArtworkPageModel(
      title = artwork.title,
      timeframe = artwork.timeframe,
      materials = artwork.materials,
      description = artwork.description.map(md => MarkdownToHtml.html(md.nodes)).orElse(Some("")),
      imageURL = artwork.imageURL
    )
  }
}