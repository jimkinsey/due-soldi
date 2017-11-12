package duesoldi.blog.pages

import java.time.format.DateTimeFormatter

import duesoldi.blog.model.BlogEntry
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtml}

object EntryPageModel
{
  def pageModel(getTwitterCard: GetEntryTwitterMetadata)
               (entry: BlogEntry): BlogEntryPageModel = {
    val title = MarkdownDocument.title(entry.content).getOrElse("-untitled-")
    BlogEntryPageModel(
      title = title,
      lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
      contentHtml = MarkdownToHtml.html(entry.content.nodes),
      twitterMetadata = getTwitterCard(entry),
      ogData = OgData(
        title = title,
        description = MarkdownDocument.text(MarkdownDocument.content(entry.content)).take(140)
      )
    )
  }
}

object BlogEntryTwitterMetadata
{
  val getTwitterCard: GetEntryTwitterMetadata = _ => Some(TwitterMetadata("summary"))
  val noTwitterCard: GetEntryTwitterMetadata = _ => None
}