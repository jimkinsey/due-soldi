package duesoldi.test.support.pages

import duesoldi.test.support.pages.DomHelper.required
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.util.control.NoStackTrace

class BlogEditingPage(html: String)
extends Page
{
  lazy val dom = Jsoup.parse(html)
  lazy val form = new BlogEditingForm(required(dom,"form.blog-entry"))
  lazy val entrySelectForm = new BlogEntrySelectForm(required(dom,"form#blog-entry-select"))
}

class BlogEntrySelectForm(element: Element)
{
  def action: String = element.attr("action")

  def entries: Seq[String] = element.select("select[name='entry'] option").asScala.map(_.`val`)

  def entry(id: String) = {
    required(element, s"select[name='entry'] option[value='$id']").attr("selected", "true")
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

class BlogEditingForm(element: Element)
{
  def action: String = element.attr("action")

  def id(id: String): BlogEditingForm = {
    required(element, "input[name='id']").`val`(id)
    this
  }

  def description(description: String): BlogEditingForm = {
    required(element, "input[name='description']").`val`(description)
    this
  }

  def content(content: String): BlogEditingForm = {
    required(element, "textarea[name='content']").`val`(content)
    this
  }

  def values: Map[String, Seq[String]] = element.select("input, textarea").asScala.map {
    input => input.attr("name") -> Seq(input.`val`())
  } toMap

  override def toString: String = element.html()
}

object DomHelper
{
  def required(element: Element, selector: String): Element = {
    Option(element.select(selector)).filter(_.asScala.nonEmpty) match {
      case Some(result) => result.first()
      case None => throw new RequiredElementMissing(selector, element.toString)
    }
  }

  class RequiredElementMissing(selector: String, fragment: String) extends RuntimeException with NoStackTrace {
    override lazy val getMessage: String = s"Required element with selector [$selector] not found in [$fragment]"
  }
}

