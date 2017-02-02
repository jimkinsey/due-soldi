package duesoldi.pages

import org.jsoup.nodes.Element

trait PageFooter { self: Page =>

  lazy val footer: Footer = new Footer(dom.select("footer").first())

  class Footer(elem: Element) {
    lazy val copyrightNotice: Option[String] = Option(elem.select("#copyright")).map(_.text())
  }
}
