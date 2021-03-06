package duesoldi.test.support.pages

import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._

trait Page
{
  def dom: Document
  final def cssUrl: String = {
    dom
      .head()
      .select("""link[rel="stylesheet"]""")
      .asScala
      .collect { case link if link.attr("href").startsWith("/") => link.attr("href") }
      .head
  }
  final override def toString: String = dom.toString
}

object Page
{
  def metaContent(name: String, dom: Document): Option[String] = Option(dom.select(s"""meta[name="$name"]""").first()).map(_.attr("content"))
  def metaProperty(name: String, dom: Document): Option[String] = Option(dom.select(s"""meta[property="$name"]""").first()).map(_.attr("content"))
}

trait Fragment {
  def elem: Element
  final override def toString: String = elem.toString
}