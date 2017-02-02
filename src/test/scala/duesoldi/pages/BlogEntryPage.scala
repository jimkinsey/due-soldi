package duesoldi.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._
import scala.xml.{Elem, XML}

class BlogEntryPage(html: String) extends Page with PageFooter {
  lazy val dom = Jsoup.parse(html)

  lazy val title = dom.title()
  lazy val h1 = dom.select("h1").first()
  lazy val content: Content = new Content(dom.select("#content").first())

  class Content(elem: Element) {
    lazy val paragraphs: Seq[Elem] = {
      elem.select("p").toSeq.map { p => XML.loadString(p.outerHtml()) }
    }
  }

}
