package duesoldi.storage

import java.io.File

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

// TODO this should be configurable!
class MarkdownSource {
  def document(id: String): Future[Option[String]] = Future successful Try(Source.fromFile(new File(s"/tmp/blog/$id.md")).mkString).toOption
}
