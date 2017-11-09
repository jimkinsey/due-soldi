package duesoldi.blog.validation

import duesoldi.markdown.MarkdownDocument

object ValidIdentifier {
  val Valid = """^([a-z0-9\-]+)$""".r
  def apply(identifier: String): Option[String] = Valid.findFirstIn(identifier)
}

object ValidBlogContent
{
  def apply(content: MarkdownDocument): Option[String] = {
    MarkdownDocument.title(content) match {
      case Some(_) => None
      case None    => Some(s"Blog content has no title (level 1 header in the Markdown)")
    }
  }
}