package duesoldi.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.xml.{Elem, XML}
import scala.collection.JavaConversions._

class BlogEntryPage(html: String) {
  private lazy val dom = Jsoup.parse(html)

  def title = dom.title()
  def h1 = dom.select("h1").first()
  def content: Content = new Content(dom.select("#content").first())
  def footer: Footer = new Footer(dom.select("footer").first())

  class Content(elem: Element) {
    lazy val paragraphs: Seq[Elem] = {
      elem.select("p").toSeq.map { p => XML.loadString(p.outerHtml()) }
    }
  }

  class Footer(elem: Element) {
    lazy val copyrightNotice: Option[String] = Option(elem.select("#copyright")).map(_.text())
  }
}
