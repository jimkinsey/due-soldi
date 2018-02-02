package duesoldi.blog.pages

import java.time.format.DateTimeFormatter

import duesoldi.blog.model.BlogEntry
import duesoldi.markdown.MarkdownDocument

object IndexPageModel
{
  def pageModel(entries: Seq[BlogEntry]) = BlogIndexPageModel(
    entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.map {
      case BlogEntry(id, content, lastModified, description) =>
        BlogIndexPageModel.Entry(
          lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
          title = MarkdownDocument.title(content).getOrElse("-untitled-"),
          id = id
        )
    }
  )
}
