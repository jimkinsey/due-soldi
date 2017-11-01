package duesoldi.page

import java.time.format.DateTimeFormatter

import duesoldi.config.Config
import duesoldi.markdown.MarkdownDocument
import duesoldi.model.BlogEntry
import duesoldi.rendering.BlogIndexPageModel

object IndexPageModel
{
  def pageModel(config: Config)
               (entries: Seq[BlogEntry]) = BlogIndexPageModel(
    entries = entries.sortBy(_.lastModified.toEpochSecond()).reverse.map {
      case BlogEntry(id, content, lastModified) =>
        BlogIndexPageModel.Entry(
          lastModified = lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
          title = MarkdownDocument.title(content).getOrElse("-untitled-"),
          id = id
        )
    },
    furnitureVersion = config.furnitureVersion
  )
}
