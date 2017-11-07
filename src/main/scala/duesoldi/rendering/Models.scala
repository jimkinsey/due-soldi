package duesoldi.rendering

trait PageModel extends Product

object BlogIndexPageModel {
  case class Entry(lastModified: String, title: String, id: String)
}
case class BlogIndexPageModel(entries: Seq[BlogIndexPageModel.Entry]) extends PageModel
case class BlogEntryPageModel(title: String, lastModified: String, contentHtml: String) extends PageModel
