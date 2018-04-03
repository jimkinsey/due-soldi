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
      ogMetadata = OgMetadata(
        title = title,
        description = entry.description.getOrElse(""),
        image = MarkdownDocument.collectFirst(entry.content.nodes) {
          case MarkdownDocument.Image(alt, src, _) => OgMetadata.Image(src, Some(alt))
        }
      )
    )
  }
}

object BlogEntryTwitterMetadata
{
  val getTwitterCard: GetEntryTwitterMetadata = _ => Some(TwitterMetadata("summary"))
  val noTwitterCard: GetEntryTwitterMetadata = _ => None
}