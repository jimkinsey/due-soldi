package duesoldi.gallery

import duesoldi.Thumbnails.GetThumbnailURL
import duesoldi.blog.storage.{BlogStore, PutBlogEntries}
import duesoldi.dependencies.DueSoldiDependencies.parseMarkdown
import duesoldi.dependencies.Injection.{Inject, inject}
import duesoldi.dependencies.{AssetStorageDependencies, JDBCDependencies, MarkdownParsingDependencies, RenderingDependencies, SessionCookieDependencies}
import duesoldi.gallery.ArtworkPageModel.SeriesModel
import duesoldi.{Thumbnails, gallery}
import duesoldi.gallery.model.{Artwork, Series}
import duesoldi.gallery.pages.{BuildArtworkPageModel, BuildGalleryHomePageModel, BuildSeriesPageModel}
import duesoldi.gallery.serialisation.{ArtworkYaml, SeriesYaml}
import duesoldi.gallery.storage._
import duesoldi.images.{ImageResize, ScalrImageResizing}

import scala.concurrent.ExecutionContext

trait GalleryDependencies
extends JDBCDependencies
with MarkdownParsingDependencies
with RenderingDependencies
with AssetStorageDependencies
with SessionCookieDependencies {

  implicit lazy val artworksFromYaml: Inject[gallery.ArtworksFromYaml] = _ => ArtworkYaml.parseAll

  implicit lazy val artworksToYaml: Inject[gallery.ArtworksToYaml] = _ => ArtworkYaml.formatAll

  implicit lazy val artworkToYaml: Inject[gallery.ArtworkToYaml] = _ => ArtworkYaml.format

  implicit lazy val artworkFromYaml: Inject[gallery.ArtworkFromYaml] = _ => ArtworkYaml.parse

  implicit lazy val seriesFromYaml: Inject[gallery.SeriesFromYaml] = _ => SeriesYaml.parse

  implicit lazy val manySeriesFromYaml: Inject[gallery.ManySeriesFromYaml] = _ => SeriesYaml.parseAll

  implicit lazy val resizeImage: Inject[ImageResize] = _ => ScalrImageResizing.imageResize()

  implicit lazy val getThumbnailURL: Inject[GetThumbnailURL] = _ => Thumbnails.getURL()

  implicit val getArtwork: Inject[GetArtwork] = { config =>
    GalleryStore.getOne(jdbcPerformQuery[Artwork](GalleryStore.toArtwork(parseMarkdown(config)))(config))
  }

  implicit val getSeries: Inject[GetSeries] = { config =>
    GalleryStore.getOneSeries(jdbcPerformQuery[Series](GalleryStore.toSeries(parseMarkdown(config)))(config))
  }

  implicit val getAllArtworks: Inject[GetAllArtworks] = { config =>
    GalleryStore.getAll(jdbcPerformQuery[Artwork](GalleryStore.toArtwork(parseMarkdown(config)))(config))
  }

  implicit val getArtworksInSeries: Inject[GetArtworksInSeries] = { config =>
    GalleryStore.getArtworksInSeries(jdbcPerformQuery[Artwork](GalleryStore.toArtwork(parseMarkdown(config)))(config))
  }

  implicit val getAllSeries: Inject[GetAllSeries] = { config =>
    GalleryStore.getAllSeries(jdbcPerformQuery[Series](GalleryStore.toSeries(parseMarkdown(config)))(config))
  }

  implicit val putArtwork: Inject[PutArtwork] = inject(GalleryStore.put _)

  implicit val putSeries: Inject[PutSeries] = inject(GalleryStore.putSeries _)

  implicit def putAllBlogEntries(implicit executionContext: ExecutionContext): Inject[PutBlogEntries] = inject(BlogStore.putAll _)

  implicit def putAllArtworks(implicit executionContext: ExecutionContext): Inject[PutArtworks] = inject(GalleryStore.putAll _)

  implicit def putAllSeries(implicit executionContext: ExecutionContext): Inject[PutManySeries] = inject(GalleryStore.putAllSeries _)

  implicit val deleteArtwork: Inject[DeleteArtwork] = inject(GalleryStore.delete _)

  implicit val deleteAllArtworks: Inject[DeleteAllArtworks] = inject(GalleryStore.deleteAll _)

  implicit def createOrUpdateArtwork(implicit executionContext: ExecutionContext): Inject[CreateOrUpdateArtwork] = inject(GalleryStore.createOrUpdate _)

  implicit val artworkPageModel: Inject[BuildArtworkPageModel] = config => ArtworkPageModel.build(config.imageBaseUrl)

  implicit val seriesPageModel: Inject[BuildSeriesPageModel] = config => GalleryHomePageModel.SeriesModel.build(config.imageBaseUrl, Thumbnails.getURL())

  implicit val galleryHomePageModel: Inject[BuildGalleryHomePageModel] = {
     config => GalleryHomePageModel.build(config.imageBaseUrl, getThumbnailURL(config))
  }

}
