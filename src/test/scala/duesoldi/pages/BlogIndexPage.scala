package duesoldi.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._

class BlogIndexPage(html: String) extends Page with PageFooter {
  lazy val dom = Jsoup.parse(html)

  lazy val blogEntries: Seq[BlogEntry] = dom.select("#content .index-entry").map(new BlogEntry(_))

  class BlogEntry(elem: Element) {
    lazy val title = elem.select("heading").text()
    lazy val link = elem.select("heading a").attr("href")
  }

}
