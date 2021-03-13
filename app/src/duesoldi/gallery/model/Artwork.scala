package duesoldi.gallery.model

import hammerspace.markdown.MarkdownDocument

import java.time.ZonedDateTime

case class Artwork(
  id: String,
  title: String,
  imageURL: String,
  description: Option[MarkdownDocument] = None,
  lastModified: ZonedDateTime = ZonedDateTime.now(),
  timeframe: Option[String] = None,
  materials: Option[String] = None,
  seriesId: Option[String] = None
)

case class Series(
  id: String,
  title: String
)