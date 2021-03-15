package duesoldi.gallery.routes

import duesoldi.app.RequestDependencies._
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.gallery.model.Series
import duesoldi.gallery.pages.{BuildArtworkPageModel, BuildGalleryHomePageModel}
import duesoldi.gallery.storage.{GetAllArtworks, GetArtwork, GetSeries}
import duesoldi.rendering.Render
import ratatoskr.ResponseBuilding._
import sommelier.handling.Unpacking._
import sommelier.routing.{Controller, Rejection, Result}
import sommelier.routing.Routing._
import sommelier.routing.SyncResult.{Accepted, Rejected}

import scala.concurrent.ExecutionContext

class GalleryHomeController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  def passIf(cond: => Boolean)(ifFalse: => Rejection): Result[Unit] = if (cond) Accepted({}) else Rejected(ifFalse)

  GET("/gallery") ->- { implicit context =>
    for {
      getAllArtworks <- provided[GetAllArtworks]
      pageModel      <- provided[BuildGalleryHomePageModel]
      rendered       <- provided[Render]

      works <- getAllArtworks() rejectWith { _ => 500 }
      _     <- passIf(works.nonEmpty) { 404 }

      model = pageModel(works)
      html <- rendered("gallery-home", model) rejectWith { f => println(f); 500 }
    } yield {
      200 (html) ContentType "text/html; charset=UTF-8"
    }
  }
}

class ArtworkController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  GET("/gallery/:id") ->- { implicit context =>
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
