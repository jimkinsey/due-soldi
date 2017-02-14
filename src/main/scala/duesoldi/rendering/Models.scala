package duesoldi.rendering

trait PageModel extends Product {
  def furnitureVersion: String
}

object BlogIndexPageModel {
  case class Entry(lastModified: String, title: String, id: String)
}
case class BlogIndexPageModel(entries: Seq[BlogIndexPageModel.Entry], furnitureVersion: String) extends PageModel

case class BlogEntryPageModel(title: String, lastModified: String, contentHtml: String, furnitureVersion: String) extends PageModel
