package duesoldi.gallery.routes

import duesoldi.app.RequestDependencies._
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.gallery.model.Series
import duesoldi.gallery.pages.{BuildArtworkPageModel, BuildGalleryHomePageModel, BuildSeriesPageModel}
import duesoldi.gallery.storage.{GetAllArtworks, GetAllSeries, GetArtwork, GetArtworksInSeries, GetSeries}
import duesoldi.rendering.Render
import ratatoskr.ResponseBuilding._
import sommelier.handling.Unpacking._
import sommelier.routing.{Controller, Rejection, Result}
import sommelier.routing.Routing._
import sommelier.routing.SyncResult.{Accepted, Rejected}

import scala.concurrent.ExecutionContext

class SeriesPageController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  GET("/gallery/series/:id") ->- { implicit context =>
    for {
      getSeries <- provided[GetSeries]
      seriesID  <- pathParam[String]("id")
      series    <- getSeries(seriesID) rejectWith { 404 }

      getArtworks <- provided[GetArtworksInSeries]
      artworks    <- getArtworks(seriesID) rejectWith { _ => 500 }

      render    <- provided[Render]
      pageModel <- provided[BuildSeriesPageModel]
      model     =  pageModel(series, artworks)
      html      <- render("series", model) rejectWith { f => println(f); 500}
    } yield {
      200(html) ContentType "text/html; charset=UTF-8"
    }
  }

}

class GalleryHomeController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  def passIf(cond: => Boolean)(ifFalse: => Rejection): Result[Unit] = if (cond) Accepted({}) else Rejected(ifFalse)

  GET("/gallery/") ->- { implicit context =>
    for {
      getAllArtworks <- provided[GetAllArtworks]
      getAllSeries   <- provided[GetAllSeries]
      pageModel      <- provided[BuildGalleryHomePageModel]
      rendered       <- provided[Render]

      works <- getAllArtworks() rejectWith { _ => 500 }
      _     <- passIf(works.nonEmpty) { 404 }

      series <- getAllSeries() rejectWith { _ => 500 }

      model = pageModel(works, series)
      html <- rendered("gallery-home", model) rejectWith { f => println(f); 500 }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}

class ArtworkController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  GET("/gallery/artwork/:id") ->- { implicit context =>
    for {
      validationFailure <- provided[ValidateIdentifier]
      getArtwork <- provided[GetArtwork]
      getSeries <- provided[GetSeries]
      pageModel <- provided[BuildArtworkPageModel]
      rendered <- provided[Render]

      validId = (id: String) => validationFailure(id).isEmpty
      id <- pathParam[String]("id").validate(validId) { 400 }
      artwork <- getArtwork(id) rejectWith { 404 }
      series <- whenAvailable(artwork.seriesId) { id =>
        getSeries(id).rejectWith { 500 } .map(Option(_))
      } (None)
      model = pageModel(artwork, series)
      html <- rendered("artwork", model) rejectWith { f => println(f); 500 }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}
