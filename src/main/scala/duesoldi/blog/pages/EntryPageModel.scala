package duesoldi.blog.pages

import java.time.format.DateTimeFormatter

import duesoldi.blog.model.BlogEntry
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}

object EntryPageModel
{
  def pageModel(entry: BlogEntry): BlogEntryPageModel = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes)
  )
}
