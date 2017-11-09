package duesoldi.blog.validation

import duesoldi.markdown.MarkdownDocument

object ValidIdentifier
{
  def apply(identifier: String): Option[String] = identifier match {
    case Valid(_) => None
    case _ => Some(s"Blog entry identifier is invalid")
  }
  private val Valid = """^([a-z0-9\-]+)$""".r
}

object ValidBlogContent
{
  def apply(content: MarkdownDocument): Option[String] = {
    MarkdownDocument.title(content) match {
      case Some(_) => None
      case None => Some(s"Blog content has no title (level 1 header in the Markdown)")
    }
  }
}