package duesoldi.model

import duesoldi.markdown.MarkdownDocument

case class BlogEntry(id: String, title: String, content: MarkdownDocument)
