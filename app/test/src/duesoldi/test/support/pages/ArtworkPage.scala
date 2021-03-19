package duesoldi.test.support.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.collection.JavaConverters._

class ArtworkPage(html: String)
extends Page
with PageFooter
{
  lazy val dom            : Document       = Jsoup.parse(html)
  lazy val title          : String         = dom.title()
  lazy val h1             : Element        = dom.select("header h1").first()
  lazy val timeframe      : Option[String] = Option(dom.select("#timeframe").text())
  lazy val materials      : Option[String] = Option(dom.select("#materials").text())
  lazy val description    : Option[String] = Option(dom.select("#description").html().trim)
  lazy val artworkImageURL: String         = dom.select("#main-image img").first().attr("src")
  lazy val seriesURL      : Option[String] = Option(dom.select("a#series-link").attr("href"))
  lazy val seriesTitle    : Option[String] = Option(dom.select("a#series-link").text())
  lazy val galleryHomeURL : String         = dom.select("a#gallery-home-link").attr("href")
}

class SeriesPage(html: String)
extends Page
with PageFooter
with OpenGraphMetaData
{
  lazy val dom   : Document = Jsoup.parse(html)
  lazy val title : String   = dom.title()
  lazy val h1    : Element  = dom.select("header h1").first()
  lazy val descriptionHTML : String = dom.select("#series-description").first().html()

  lazy val works : Seq[SeriesPage.Work] = dom.select(".work").asScala.map(e => new SeriesPage.Work(e))

  def workTitled(title: String): SeriesPage.Work = {
    works.find(_.title == title).get
  }
}

object SeriesPage {
  class Work(val elem: Element) extends Fragment {
    lazy val title: String = elem.select("a.artwork-link").text().trim
    lazy val link: String = elem.select("a").attr("href")
    lazy val thumbnailURL: String = elem.select("img").attr("src")
  }
}

class GalleryHomePage(html: String)
extends Page
with PageFooter
with OpenGraphMetaData
{
  lazy val dom                 : Document    = Jsoup.parse(html)
  lazy val title               : String      = dom.title()
  lazy val h1                  : Element     = dom.select("header h1").first()
  lazy val artworkLinks        : Seq[String] = dom.select("a.artwork-link").asScala.map(_.attr("href"))
  lazy val artworkThumbnailURLs: Seq[String] = dom.select("img#thumbnail").asScala.map(_.attr("src"))

  def seriesNamed(name: String): Option[GalleryHomePage.SeriesSection] = {
    dom.select(".series").asScala.map(e => new GalleryHomePage.SeriesSection(e)).find(_.title.trim == name.trim)
  }
}

object GalleryHomePage {

  class SeriesSection(val elem: Element) extends Fragment {
    lazy val title: String                  = elem.select("header").text()
    lazy val works: Seq[SeriesSection.Work] = elem.select(".work").asScala.map(e => new SeriesSection.Work(e))
  }

  object SeriesSection {
    class Work(val elem: Element) extends Fragment {
      lazy val title: String = elem.select("a.artwork-link").text()
    }
  }
}