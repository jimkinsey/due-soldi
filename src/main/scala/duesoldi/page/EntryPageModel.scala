package duesoldi.page

import java.time.format.DateTimeFormatter

import duesoldi.config.Config
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtmlConverter}
import duesoldi.model.BlogEntry
import duesoldi.rendering.BlogEntryPageModel

object EntryPageModel {
  def pageModel(config: Config)
               (entry: BlogEntry): BlogEntryPageModel = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtmlConverter.html(entry.content.nodes),
    furnitureVersion = config.furnitureVersion
  )
}
