package duesoldi.pages

import org.jsoup.nodes.Document

trait Page {
  def dom: Document
}
