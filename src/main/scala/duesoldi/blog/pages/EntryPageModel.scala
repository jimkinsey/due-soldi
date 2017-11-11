package duesoldi.blog.pages

import java.time.format.DateTimeFormatter

import duesoldi.blog.model.BlogEntry
import duesoldi.markdown.{MarkdownDocument, MarkdownToHtml}

object EntryPageModel
{
  def pageModel(getTwitterCard: GetEntryTwitterCard)
               (entry: BlogEntry): BlogEntryPageModel = BlogEntryPageModel(
    title = MarkdownDocument.title(entry.content).getOrElse("-untitled-"),
    lastModified = entry.lastModified.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
    contentHtml = MarkdownToHtml.html(entry.content.nodes),
    twitterCard = getTwitterCard(entry)
  )
}

object BlogEntryTwitterCard
{
  val getTwitterCard: GetEntryTwitterCard = (entry) => {
    for {
      title <- MarkdownDocument.title(entry.content)
      description = MarkdownDocument.text(MarkdownDocument.content(entry.content)).take(140)
    } yield {
      TwitterCard(title, description)
    }
  }

  val noTwitterCard: GetEntryTwitterCard = _ => None
}