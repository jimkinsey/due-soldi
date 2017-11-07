package duesoldi.page

import java.time.format.DateTimeFormatter

import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.model.BlogEntry
import duesoldi.rendering.BlogEntryPageModel

object EntryPageModel {
  def pageModel(entry: BlogEntry): BlogEntryPageModel = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes)
  )
}
