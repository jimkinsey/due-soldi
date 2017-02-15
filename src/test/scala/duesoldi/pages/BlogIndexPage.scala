package duesoldi.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._

class BlogIndexPage(html: String) extends Page with PageFooter {
  lazy val dom = Jsoup.parse(html)

  lazy val title = dom.title()
  lazy val heading = dom.select("h1").text()
  lazy val blogEntries: Seq[BlogEntry] = dom.select("#blog-index article").map(new BlogEntry(_))
  lazy val blurb = new Blurb(dom.select("#blurb").head)

  class BlogEntry(elem: Element) {
    lazy val title = elem.select("header h2").text()
    lazy val link = elem.select("header a").attr("href")
    lazy val date = elem.select("header time").text()
  }

  class Blurb(element: Element) {
    lazy val title = element.select("header h2").text()
    lazy val paragraphs = element.select("p")
  }

}
