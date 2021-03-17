package duesoldi.test.support.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

class ArtworkPage(html: String)
extends Page
with PageFooter
{
  lazy val dom: Document = Jsoup.parse(html)
  lazy val title: String = dom.title()
  lazy val h1: Element = dom.select("header h1").first()
  lazy val timeframe: Option[String] = Option(dom.select("#timeframe").text())
  lazy val materials: Option[String] = Option(dom.select("#materials").text())
  lazy val description: Option[String] = Option(dom.select("#description").html().trim)
  lazy val artworkImageURL: String = dom.select("#main-image img").first().attr("src")
  lazy val seriesURL: Option[String] = Option(dom.select("a#series-link").attr("href"))
  lazy val seriesTitle: Option[String] = Option(dom.select("a#series-link").text())
}

class GalleryHomePage(html: String)
extends Page
with PageFooter
{

  import scala.collection.JavaConverters._

  lazy val dom: Document = Jsoup.parse(html)
  lazy val title: String = dom.title()
  lazy val h1: Element = dom.select("header h1").first()
  lazy val artworkLinks: Seq[String] = dom.select("a#artwork-link").asScala.map(_.attr("href"))
  lazy val artworkThumbnailURLs: Seq[String] = dom.select("img#thumbnail").asScala.map(_.attr("src"))
}