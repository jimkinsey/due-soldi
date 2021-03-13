package duesoldi.test.support.setup

import duesoldi.Env
import duesoldi.test.support.app.ServerRequests
import duesoldi.test.support.httpclient.BasicAuthorization

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object ConfigOverride
{
  def configOverride(vars: (String, String)*) = new SyncSetup {
    override def setup(env: Env): Env = Map(vars:_*)
  }
}

object GalleryStorage
{
  val artwork = ArtworkBuilder()
  val series = SeriesBuilder()

  case class ArtworkBuilder(
    id: String = "id",
    title: String = "Title",
    lastModified: ZonedDateTime = ZonedDateTime.now(),
    description: Option[String] = None,
    timeframe: Option[String] = None,
    imageURL: String = "/path/to/image.png",
    materials: Option[String] = None,
    seriesID: Option[String] = None
  ) {
    def withId(id: String): ArtworkBuilder = copy(id = id)
    def withDescription(description: String): ArtworkBuilder = copy(description = Some(description))
    def withTitle(title: String): ArtworkBuilder = copy(title = title)
    def withTimeframe(timeframe: String): ArtworkBuilder = copy(timeframe = Some(timeframe))
    def withImageURL(url: String): ArtworkBuilder = copy(imageURL = url)
    def withMaterials(materials: String): ArtworkBuilder = copy(materials = Some(materials))
    def belongingToSeries(id: String): ArtworkBuilder = copy(seriesID = Some(id))

    lazy val toYaml: String = {
      s"""id: $id
         |title: $title
         |timeframe: ${timeframe.getOrElse("")}
         |materials: ${materials.getOrElse("")}
         |image-url: $imageURL
         |last-modified: ${lastModified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}
         |series-id: ${seriesID.getOrElse("")}
         |description: |
         |${description.getOrElse("").indented}
         """.stripMargin
    }
  }

  case class SeriesBuilder(
    id: String = "id",
    title: String = "title"
  ) {
    def withId(id: String): SeriesBuilder = copy(id = id)
    def withTitle(title: String): SeriesBuilder = copy(title = title)

    lazy val toYaml: String = {
      s"""id: $id
         |title: $title
         """.stripMargin
    }
  }

  implicit class YamlOps(yaml: String) {
    def indented: String = {
      yaml.lines.map(line => s"    $line").mkString("\n")
    }
  }

  implicit def tupleToBuilder(tuple: (String, String)): ArtworkBuilder = tuple match {
    case (id, title) => ArtworkBuilder(id = id, title = title)
  }

  implicit def tuple3ToBuilder(tuple: (String, String, String)): ArtworkBuilder = tuple match {
    case (id, title, imageURL) => ArtworkBuilder(id = id, title = title, imageURL = imageURL)
  }

  def artworks(entries: ArtworkBuilder*)(implicit executionContext: ExecutionContext) = new AsyncSetup {
    override def setup(env: Env): Future[Env] = {
      implicit val e: Env = env
      val user :: password :: Nil = env("ADMIN_CREDENTIALS").split(":").toList
      Future.sequence(
        entries.map(entry => ServerRequests.put(s"/admin/artwork/${entry.id}", entry.toYaml, BasicAuthorization(user, password)))
      ) map ( _ => Map.empty )
    }
  }

  def series(series: SeriesBuilder*)(implicit executionContext: ExecutionContext) = new AsyncSetup {
    override def setup(env: Env): Future[Env] = {
      implicit val e: Env = env
      val user :: password :: Nil = env("ADMIN_CREDENTIALS").split(":").toList
      Future.sequence(
        series.map(series => ServerRequests.put(s"/admin/series/${series.id}", series.toYaml, BasicAuthorization(user, password)))
      ) map ( _ => Map.empty )
    }
  }
}
