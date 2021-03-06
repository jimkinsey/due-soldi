package duesoldi.test.support.pages

import duesoldi.test.support.pages.Page.{metaContent, metaProperty}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._

class BlogEntryPage(html: String)
extends Page
with PageFooter
{
  lazy val dom: Document = Jsoup.parse(html)
  lazy val title: String = dom.title()
  lazy val h1: Element = dom.select("header h1").first()
  lazy val content: Content = new Content(dom.select("#content").first())
  lazy val date: String = dom.select("header time").text()
  lazy val navigation: Navigation = new Navigation(dom.select("nav").asScala.head)
  lazy val twitterMetadata = TwitterMetadata(dom)
  lazy val ogMetadata = OgMetadata(dom)
  lazy val description = Option(dom.select("#description").first().text())
}

object TwitterMetadata
{
  def apply(dom: Document): Option[TwitterMetadata] = {
    for {
      card <- metaContent("twitter:card", dom)
    } yield {
      TwitterMetadata(card)
    }
  }
}

case class TwitterMetadata(card: String)

trait OpenGraphMetaData { self: Page =>
  lazy val ogMetadata = OgMetadata(dom)
}

object OgMetadata
{
  def apply(dom: Document): Option[OgMetadata] = {
    for {
      title <- metaProperty("og:title", dom)
      description = metaProperty("og:description", dom)
    } yield {
      OgMetadata(title, description, Image(dom))
    }
  }

  case class Image(url: String, alt: Option[String])

  object Image
  {
    def apply(dom: Document): Option[Image] = {
      for {
        url <- metaProperty("og:image:url", dom).orElse(metaProperty("og:image", dom))
        alt = metaProperty("og:image:alt", dom)
      } yield {
        Image(url, alt)
      }
    }
  }
}

case class OgMetadata(title: String, description: Option[String], image: Option[OgMetadata.Image])

class Navigation(elem: Element)
{
  lazy val items: Seq[Item] = elem.select("ol > li").asScala.map(li => new Item(li))
}

class Item(element: Element)
{
  lazy val url: String = element.select("a").attr("href")
}

class Content(elem: Element)
{
  lazy val paragraphs: Seq[String] = elem.select("p").asScala.map { _.outerHtml() }
  lazy val images: Seq[Image] = elem.select("img").asScala.map { i => new Image(i) }
}

class Image(elem: Element)
{
  lazy val src: String = elem.attr("src")
  lazy val alt: String = elem.attr("alt")
  lazy val title: Option[String] = Option(elem.attr("title"))
}
