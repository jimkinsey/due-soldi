package duesoldi.pages

import org.jsoup.Jsoup

/**
  * Created by jimkinsey on 19/01/17.
  */
class BlogEntryPage(html: String) {
  private lazy val dom = Jsoup.parse(html)
  def title = dom.title()
  def h1 = dom.select("h1").first()
}
