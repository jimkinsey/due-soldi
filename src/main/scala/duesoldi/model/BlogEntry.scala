package duesoldi.model

import java.time.LocalDateTime

import duesoldi.markdown.MarkdownDocument

case class BlogEntry(id: String, title: String, content: MarkdownDocument, lastModified: LocalDateTime)
