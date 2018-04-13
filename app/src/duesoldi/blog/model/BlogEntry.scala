package duesoldi.blog.model

import java.time.ZonedDateTime

import hammerspace.markdown.MarkdownDocument

case class BlogEntry(
  id: String,
  content: MarkdownDocument,
  lastModified: ZonedDateTime = ZonedDateTime.now(),
  description: Option[String] = None
)
