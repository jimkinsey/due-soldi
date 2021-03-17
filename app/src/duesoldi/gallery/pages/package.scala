package duesoldi.gallery

import duesoldi.Thumbnails.GetThumbnailURL
import duesoldi.blog.pages.{GetEntryTwitterMetadata, PageModel}
import duesoldi.gallery.ArtworkPageModel.SeriesModel
import duesoldi.gallery.GalleryHomePageModel.SeriesModel.ArtworkModel
import duesoldi.gallery.model.{Artwork, Series}
import duesoldi.gallery.pages.BuildArtworkPageModel
import hammerspace.markdown.MarkdownToHtml

package object pages {
  type BuildArtworkPageModel = (Artwork, Option[Series]) => ArtworkPageModel
  type BuildGalleryHomePageModel = (Seq[Artwork], Seq[Series]) => GalleryHomePageModel
}

case class GalleryHomePageModel(
  series: Seq[GalleryHomePageModel.SeriesModel]
) extends PageModel

object GalleryHomePageModel {
  def build(
    imageBaseURL: String,
    getThumbnailURL: GetThumbnailURL
  )(works: Seq[Artwork], allSeries: Seq[Series]): GalleryHomePageModel = {
    // FIXME there's some repetition in here and it feels kind of clumsy...
    val groupedWorks = works.groupBy(_.seriesId)

    GalleryHomePageModel(
      series = groupedWorks.map {
        case (Some(seriesID), works) if allSeries.exists(_.id == seriesID) =>
          val series = allSeries.find(_.id == seriesID).get
          SeriesModel(
            id = seriesID,
            title = series.title,
            artworks = works.map( work => ArtworkModel(
              title = work.title,
              url = s"/gallery/${work.id}",
              thumbnailURL = imageBaseURL + getThumbnailURL(work.imageURL)
            ))
          )
        case (None, works) =>
          SeriesModel(
            id = "misc",
            title = "Miscellaneous works",
            artworks = works.map( work => ArtworkModel(
              title = work.title,
              url = s"/gallery/${work.id}",
              thumbnailURL = imageBaseURL + getThumbnailURL(work.imageURL)
            ))
          )
      } toSeq
    )
  }

  case class SeriesModel(
    id: String,
    title: String,
    artworks: Seq[SeriesModel.ArtworkModel]
  )

  object SeriesModel {
    case class ArtworkModel(title: String, url: String, thumbnailURL: String)
  }
}

case class ArtworkPageModel(
  title: String,
  timeframe: Option[String],
  materials: Option[String],
  description: Option[String],
  imageURL: String,
  series: Option[SeriesModel]
) extends PageModel

object ArtworkPageModel {
  def build(
    imageBaseURL: String
  )(artwork: Artwork, series: Option[Series]): ArtworkPageModel = {
    ArtworkPageModel(
      title = artwork.title,
      timeframe = artwork.timeframe,
      materials = artwork.materials,
      description = artwork.description.map(md => MarkdownToHtml.html(md.nodes)).orElse(Some("")),
      imageURL = imageBaseURL + artwork.imageURL,
      series = series.map(SeriesModel.build())
    )
  }

  case class SeriesModel(url: String, title: String)

  object SeriesModel {
    def build()(series: Series): SeriesModel = {
      SeriesModel(s"/gallery/series/${series.id}", series.title)
    }
  }
}