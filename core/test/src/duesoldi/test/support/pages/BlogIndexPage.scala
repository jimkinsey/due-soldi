package duesoldi.test.support.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

class BlogIndexPage(html: String)
  extends Page
  with PageFooter
{
  lazy val dom: Document = Jsoup.parse(html)
  lazy val title: String = dom.title()
  lazy val heading: String = dom.select("h1").text()
  lazy val blogEntries: Seq[BlogEntry] = dom.select("#blog-index article").asScala.map(new BlogEntry(_))
  lazy val blurb: Blurb = new Blurb(dom.select("#blurb").asScala.head)
}

class BlogEntry(elem: Element)
{
  lazy val title: String = elem.select("header h2").text()
  lazy val link: String = elem.select("header a").attr("href")
  lazy val date: String = elem.select("header time").text()
}

class Blurb(element: Element)
{
  lazy val title: String = element.select("header h2").text()
  lazy val paragraphs: Elements = element.select("p")
}
