package duesoldi.blog.pages

trait PageModel extends Product

object BlogIndexPageModel
{
  case class Entry(lastModified: String, title: String, id: String)
}

case class BlogIndexPageModel(entries: Seq[BlogIndexPageModel.Entry]) extends PageModel

case class BlogEntryPageModel(
                               title: String,
                               lastModified: String,
                               contentHtml: String,
                               ogData: OgData,
                               twitterMetadata: Option[TwitterMetadata] = None
) extends PageModel

case class TwitterMetadata(card: String)

case class OgData(title: String, description: String)
