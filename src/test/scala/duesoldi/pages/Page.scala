package duesoldi.pages

import org.jsoup.nodes.Document

import scala.collection.JavaConversions._

trait Page {
  def dom: Document
  final def cssUrl: String = dom.head().select("""link[rel="stylesheet"]""").toSeq.map(_.attr("href")).find(_.startsWith("/")).getOrElse(???)
  final override def toString: String = dom.toString
}
