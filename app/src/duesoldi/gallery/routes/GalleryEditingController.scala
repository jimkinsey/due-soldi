package duesoldi.gallery.routes

import duesoldi.app.AdminAuth.basicAdminAuth
import duesoldi.app.RequestDependencies._
import duesoldi.app.sessions.Sessions.{GetSessionCookie, validSession}
import duesoldi.assets.StoreAsset
import duesoldi.blog.validation.ValidateIdentifier
import duesoldi.config.Config
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.gallery.model.Artwork
import duesoldi.gallery.pages.ArtworkEditingPageModel
import duesoldi.gallery.storage._
import duesoldi.gallery.{ArtworkFromYaml, ArtworkToYaml, ArtworksFromYaml, ArtworksToYaml, ManySeriesFromYaml, SeriesFromYaml}
import duesoldi.rendering.Render
import hammerspace.markdown
import ratatoskr.ResponseBuilding._
import sommelier.handling.Unpacking._
import sommelier.routing.Routing._
import sommelier.routing.{Controller, Result}

import scala.concurrent.ExecutionContext


class GalleryEditingController(implicit executionContext: ExecutionContext, appConfig: Config)
extends Controller
{

  // TODO id non-editable (for now)
  // TODO POST result needs to have some data in it - make a reusable method for the whole thing?

  GET("/admin/artwork/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500

      getArtwork <- provided[GetArtwork]
      selectedArtworkId <- query[String]("artwork").optional.firstValue

      emptyArtwork = Artwork(
        id = "",
        title = "",
        imageURL = ""
      )
      artwork <- if (selectedArtworkId.isDefined) {
        getArtwork(selectedArtworkId.get) defaultTo emptyArtwork
      } else {
        Result(emptyArtwork)
      }

      getArtworks <- provided[GetAllArtworks]
      artworks <- getArtworks() rejectWith { _ => 500 }
      model = ArtworkEditingPageModel(
        artworks = artworks.map(work =>
          ArtworkEditingPageModel.Artwork(
            id = work.id,
            title = work.title,
            timeframe = work.timeframe.getOrElse(""),
            materials = work.materials.getOrElse(""),
            imageURL = work.imageURL,
            description = work.description.map(_.raw).getOrElse("")
          )
        ),
        artwork = ArtworkEditingPageModel.Artwork(
          id = artwork.id,
          title = artwork.title,
          timeframe = artwork.timeframe.getOrElse(""),
          materials = artwork.materials.getOrElse(""),
          imageURL = artwork.imageURL,
          description = artwork.description.map(_.raw).getOrElse("")
        )
      )

      render <- provided[Render]
      html <- render("artwork-editing", model) rejectWith { failure => 500(s"Failed to render $failure") }
    } yield {
      200(html) cookie sessionCookie ContentType "text/html; charset=UTF-8"
    }
  }

  POST("/admin/artwork/edit").Authorization(basicAdminAuth or validSession) ->- { implicit context =>
    for {
      getSessionCookie <- provided[GetSessionCookie]
      sessionCookie <- getSessionCookie(context.request) rejectWith 500

      _ = println("Getting data from form...")

      parseMarkdown <- provided[markdown.Parse]
      id <- form[String]("id").firstValue.required { 500 }
      title <- form[String]("title").firstValue.required { 500 }
      imageURL <- form[String]("image-url").firstValue.required { 500 }
      description <- form[String]("description").optional.firstValue
      timeframe <- form[String]("timeframe").optional.firstValue
      materials <- form[String]("materials").optional.firstValue
      artwork = Artwork(
        id,
        title = title,
        imageURL = imageURL,
        materials = materials,
        timeframe = timeframe,
        description = description.map(parseMarkdown)
      )

      _ = println("Getting uploaded file...")

      imageFile <- uploadedFiles("image").optional.firstValue
      storeAsset <- provided[StoreAsset]
      _ <- whenAvailable(imageFile) { f => storeAsset(imageURL, f.data) rejectWith { _ => 500 } } ({})

      _ = println("Storing artwork...")

      store <- provided[CreateOrUpdateArtwork]
      _ <- store(artwork) rejectWith { _ => 500 }

      _ = println("Getting artworks to build view model...")

      getArtworks <- provided[GetAllArtworks]
      artworks <- getArtworks() rejectWith { _ => 500 }
      model = ArtworkEditingPageModel(
        artworks = artworks.map(work =>
          ArtworkEditingPageModel.Artwork(
            id = work.id,
            title = work.title,
            timeframe = work.timeframe.getOrElse(""),
            materials = work.materials.getOrElse(""),
            imageURL = work.imageURL,
            description = work.description.map(_.raw).getOrElse("")
          )
        ),
        artwork = ArtworkEditingPageModel.Artwork(
          id = artwork.id,
          title = artwork.title,
          timeframe = artwork.timeframe.getOrElse(""),
          materials = artwork.materials.getOrElse(""),
          imageURL = artwork.imageURL,
          description = artwork.description.map(_.raw).getOrElse("")
        )
      )

      _ = println("Rendering HTML...")

      render <- provided[Render]
      html <- render("artwork-editing", model) rejectWith { failure => 500 }
    } yield {
      201(html) cookie sessionCookie ContentType "text/html; charset=UTF-8"
    }
  }

  PUT("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      parse              <- provided[ArtworkFromYaml]
      putArtwork         <- provided[PutArtwork]
      getArtwork         <- provided[GetArtwork]

      id      <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      content <- body[String]
      _       <- getArtwork(id).map(_.toLeft(false)) rejectWith { _ => 409 (s"Artwork with ID '$id' already exists")}
      artwork <- parse(content) rejectWith { failure => 400 (s"Failed to parse this content [$failure] [$content]")}
      _       <- putArtwork(artwork) rejectWith { _ => 500 ("Failed to store artwork") }
    } yield {
      201 ("")
    }
  }

  PUT("/admin/series/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      parse              <- provided[SeriesFromYaml]
      putSeries          <- provided[PutSeries]
      getSeries          <- provided[GetSeries]


      id      <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      _ = println(s"PUTTING SERIES WITH ID $id")

      content <- body[String]
      _       <- getSeries(id).map(_.toLeft(false)) rejectWith { _ => 409 (s"Series with ID '$id' already exists")}
      series  <- parse(content) rejectWith { failure => 400 (s"Failed to parse this content [$failure] [$content]")}
      _       <- putSeries(series) rejectWith { _ => 500 ("Failed to store series") }
    } yield {
      println("SUCCESSFULLY PUT SERIES")
      201 ("")
    }
  }

  DELETE("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      deleteArtwork      <- provided[DeleteArtwork]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      _  <- deleteArtwork(id) rejectWith { _ => 500 ("Failed to delete blog entry")}
    } yield {
      204
    }
  }

  GET("/admin/artwork/:id").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      validateIdentifier <- provided[ValidateIdentifier]
      getArtwork <- provided[GetArtwork]
      format <- provided[ArtworkToYaml]

      id <- pathParam[String]("id").validate(id => validateIdentifier(id).isEmpty) { 400 ("Invalid identifier") }
      artwork <- getArtwork(id) rejectWith { 404 }
    } yield {
      200 (format(artwork))
    }
  }

  GET("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      getArtworks <- provided[GetAllArtworks]
      format <- provided[ArtworksToYaml]

      artworks <- getArtworks()
    } yield {
      200 (format(artworks))
    }
  }

  PUT("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      putArtworks <- provided[PutArtworks]
      parse       <- provided[ArtworksFromYaml]

      content  <- body[String]
      artworks <- parse(content) rejectWith { failure => 400 (s"Failed to parse document - $failure") }
      _        <- putArtworks(artworks) rejectWith { failure => 500 (s"Failed to store artworks - $failure") }
    } yield {
      201
    }
  }

  DELETE("/admin/artwork").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      deleteWorks <- provided[DeleteAllArtworks]

      _ <- deleteWorks() rejectWith { _ => 500 }
    } yield {
      204
    }
  }

  PUT("/admin/series").Authorization(basicAdminAuth) ->- { implicit context =>
    for {
      putSeries <- provided[PutManySeries]
      parse     <- provided[ManySeriesFromYaml]

      content <- body[String]
      series  <- parse(content) rejectWith { failure => 400 (s"Failed to parse document - $failure") }

      _ = println(s"Parsed series: [$series]")

      _       <- putSeries(series) rejectWith { failure => 500 (s"Failed to store series - $failure") }
    } yield {

      println(s"Successfully stored series [${series}]")

      201
    }
  }

}
