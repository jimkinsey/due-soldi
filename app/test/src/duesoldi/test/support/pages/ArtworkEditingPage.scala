package duesoldi.test.support.pages

import duesoldi.test.support.pages.ArtworkEditingForm.Series
import duesoldi.test.support.pages.DomHelper.required
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.util.control.NoStackTrace

class ArtworkEditingPage(html: String)
extends Page
{
  lazy val dom = Jsoup.parse(html)
  lazy val form = new ArtworkEditingForm(required(dom,"form.artwork"))
  lazy val artworkSelectForm = new ArtworkSelectForm(required(dom,"form#artwork-select"))
}

class ArtworkSelectForm(element: Element)
{
  def method: String = element.attr("method")

  def action: String = element.attr("action")

  def entries: Seq[String] = element.select("select[name='artwork'] option").asScala.map(_.`val`)

  def entry(id: String) = {
    required(element, s"select[name='artwork'] option[value='$id']").attr("selected", "true")
    this
  }

  def values: Map[String, Seq[String]] = {
    element.select("input, textarea, select").asScala.map {
      case formEntry if formEntry.tagName() == "select" =>
        formEntry.attr("name") -> Option(formEntry.select("option[selected]").first()).map(_.`val`).map(s => Seq(s)).getOrElse(Seq.empty)
      case input =>
        input.attr("name") -> Seq(input.`val`())
    } toMap
  }

  override def toString: String = element.html()
}

class ArtworkEditingForm(element: Element)
{
  def action: String = element.attr("action")

  def id(id: String): ArtworkEditingForm = {
    required(element, "input[name='id']").`val`(id)
    this
  }

  def title(date: String): ArtworkEditingForm = {
    required(element, "input[name='title']").`val`(date)
    this
  }

  def description(description: String): ArtworkEditingForm = {
    required(element, "textarea[name='description']").`val`(description)
    this
  }

  def timeframe(content: String): ArtworkEditingForm = {
    required(element, "input[name='timeframe']").`val`(content)
    this
  }

  def materials(content: String): ArtworkEditingForm = {
    required(element, "input[name='materials']").`val`(content)
    this
  }

  def imageURL(content: String): ArtworkEditingForm = {
    required(element, "input[name='image-url']").`val`(content)
    this
  }

  def values: Map[String, Seq[String]] = {
    element.select("input, textarea, select").asScala.map {
      case formEntry if formEntry.tagName() == "select" =>
        formEntry.attr("name") -> Option(formEntry.select("option[selected]").first()).map(_.`val`).map(s => Seq(s)).getOrElse(Seq.empty)
      case input =>
        input.attr("name") -> Seq(input.`val`())
    } toMap
  }

  def acceptCharset: String = element.attr("accept-charset")

  def availableSeries: List[ArtworkEditingForm.Series] =
    element.select("select[name='series'] option").asScala.map(e => new Series(e)).toList

  def seriesID(id: String): ArtworkEditingForm = {
    element.select("select[name='series'] option").asScala.foreach(_.removeAttr("selected"))
    required(element, s"select[name='series'] option[value='$id']").attr("selected", "true")
    this
  }

  def newSeriesID(id: String): ArtworkEditingForm = {
    required(element, "input[name='new-series-id']").`val`(id)
    this
  }

  def newSeriesTitle(title: String): ArtworkEditingForm = {
    required(element, "input[name='new-series-title']").`val`(title)
    this
  }

  def newSeriesDescription(description: String): ArtworkEditingForm = {
    required(element, "textarea[name='new-series-description']").`val`(description)
    this
  }

  override def toString: String = element.html()
}

object ArtworkEditingForm {

  class Series(elem: Element) {
    lazy val title: String = elem.text()
    lazy val id: String = elem.`val`()
  }

}