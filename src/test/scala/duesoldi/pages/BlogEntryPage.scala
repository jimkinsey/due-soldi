package duesoldi.pages

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

class BlogEntryPage(html: String)
extends Page
with PageFooter
{
  lazy val dom: Document = Jsoup.parse(html)
  lazy val title: String = dom.title()
  lazy val h1: Element = dom.select("h1").first()
  lazy val content: Content = new Content(dom.select("#content").first())
  lazy val date: String = dom.select("header time").text()
  lazy val navigation: Navigation = new Navigation(dom.select("nav").head)
}

class Navigation(elem: Element)
{
  lazy val items: Seq[Item] = elem.select("ol > li").map(li => new Item(li))
}

class Item(element: Element)
{
  lazy val url: String = element.select("a").attr("href")
}

class Content(elem: Element)
{
  lazy val paragraphs: Seq[String] = elem.select("p").toSeq.map { _.outerHtml() }
  lazy val images: Seq[Image] = elem.select("img").toSeq.map { i => new Image(i) }
}

class Image(elem: Element)
{
  lazy val src: String = elem.attr("src")
  lazy val alt: String = elem.attr("alt")
  lazy val title: Option[String] = Option(elem.attr("title"))
}
