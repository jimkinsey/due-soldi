package duesoldi.pages

import org.jsoup.nodes.Document

trait Page {
  def dom: Document
  override def toString: String = dom.toString
}
