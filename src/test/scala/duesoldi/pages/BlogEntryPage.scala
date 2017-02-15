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
  lazy val date = dom.select("header time").text()
  lazy val navigation = new Navigation(dom.select("nav").head)

  class Content(elem: Element) {
    lazy val paragraphs: Seq[Elem] = {
      elem.select("p").toSeq.map { p => XML.loadString(p.outerHtml()) }
    }
  }

  class Navigation(elem: Element) {
    lazy val items: Seq[Item] = elem.select("ol > li").map(li => new Item(li))

    class Item(element: Element) {
      lazy val url = element.select("a").attr("href")
    }
  }

}
