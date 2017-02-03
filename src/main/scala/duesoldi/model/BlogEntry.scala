package duesoldi.model

import java.time.ZonedDateTime

import duesoldi.markdown.MarkdownDocument

case class BlogEntry(id: String, title: String, content: MarkdownDocument, lastModified: ZonedDateTime)
