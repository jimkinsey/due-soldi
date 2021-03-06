package duesoldi.gallery.pages

import duesoldi.blog.pages.PageModel

case class SeriesEditingPageModel(
  allSeries: Seq[SeriesEditingPageModel.Series],
  series: SeriesEditingPageModel.Series
) extends PageModel

object SeriesEditingPageModel {
  case class Series(
    id: String,
    title: String,
    description: String
  )
}

case class ArtworkEditingPageModel(
  artworks: Seq[ArtworkEditingPageModel.Artwork],
  artwork: ArtworkEditingPageModel.Artwork,
) extends PageModel

object ArtworkEditingPageModel
{
  case class Artwork(
    id: String,
    title: String,
    timeframe: String,
    materials: String,
    imageURL: String,
    description: String,
    allSeries: Seq[ArtworkEditingPageModel.Series] = Seq.empty
)

  case class Series(
    id: String,
    title: String
  )
}