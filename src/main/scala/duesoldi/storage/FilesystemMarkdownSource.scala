package duesoldi.storage

import java.io.File

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

trait MarkdownSource {
  def document(id: String): Future[Option[String]]
}

object FilesystemMarkdownSource {
  trait Config {
    def path: String
  }
}

class FilesystemMarkdownSource(implicit config: FilesystemMarkdownSource.Config) extends MarkdownSource {
  def document(id: String): Future[Option[String]] =
    Future successful Try(Source.fromFile(new File(s"${config.path}/$id.md")).mkString).toOption
}
