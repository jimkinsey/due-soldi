package duesoldi.blog.model

import java.time.ZonedDateTime

import duesoldi.markdown.MarkdownDocument

case class BlogEntry(id: String, content: MarkdownDocument, lastModified: ZonedDateTime = ZonedDateTime.now())
