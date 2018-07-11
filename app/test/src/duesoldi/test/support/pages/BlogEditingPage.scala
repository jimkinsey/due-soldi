package duesoldi.test.support.pages

import duesoldi.test.support.pages.DomHelper.required
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.util.control.NoStackTrace

class BlogEditingPage(html: String)
extends Page
{
  lazy val dom: Document = Jsoup.parse(html)
  lazy val form: BlogEditingForm = new BlogEditingForm(required(dom,"form.blog-entry"))
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
    required(element, "input[name='content']").`val`(content)
    this
  }

  def values: Map[String, Seq[String]] = element.select("input").asScala.map {
    input => input.attr("name") -> Seq(input.`val`())
  } toMap
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

