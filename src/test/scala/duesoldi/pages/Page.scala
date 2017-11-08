package duesoldi.pages

import org.jsoup.nodes.Document

import scala.collection.JavaConversions._

trait Page {
  def dom: Document
  final def cssUrl: String = {
    dom
      .head()
      .select("""link[rel="stylesheet"]""")
      .toSeq
      .collect { case link if link.attr("href").startsWith("/") => link.attr("href") }
      .head
  }
  final override def toString: String = dom.toString
}
