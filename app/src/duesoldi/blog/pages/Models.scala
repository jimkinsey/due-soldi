package duesoldi.blog.pages

trait PageModel extends Product

object BlogIndexPageModel
{
  case class Entry(lastModified: String, title: String, id: String, description: String)
}

case class BlogIndexPageModel(entries: Seq[BlogIndexPageModel.Entry]) extends PageModel

case class BlogEntryPageModel(
  title: String,
  lastModified: String,
  contentHtml: String,
  ogMetadata: OgMetadata,
  twitterMetadata: Option[TwitterMetadata] = None,
  description: Option[String] = None
) extends PageModel

case class TwitterMetadata(card: String)

object OgMetadata
{
  case class Image(url: String, alt: Option[String] = None)
}

case class OgMetadata(
  title: String,
  description: String,
  image: Option[OgMetadata.Image] = None
)

case class BlogEditingPageModel() extends PageModel