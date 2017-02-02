package duesoldi.storage

import java.io.File
import java.time.{LocalDateTime, ZoneOffset}

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

case class MarkdownContainer(lastModified: LocalDateTime = LocalDateTime.now(), content: String)

trait MarkdownSource {
  def document(id: String): Future[Option[MarkdownContainer]]
  def documents: Future[Seq[(String, MarkdownContainer)]]
}

object FilesystemMarkdownSource {
  trait Config {
    def path: String
  }
}

class FilesystemMarkdownSource(implicit config: FilesystemMarkdownSource.Config) extends MarkdownSource {
  override def document(id: String): Future[Option[MarkdownContainer]] =
    Future successful Try(MarkdownContainer(content = Source.fromFile(new File(s"${config.path}/$id.md")).mkString)).toOption

  override def documents: Future[Seq[(String, MarkdownContainer)]] = {
    Future successful {
      Try({new File(config.path).listFiles().filter(_.getName.endsWith("md")).map { file =>
        file.getName.dropRight(3) -> MarkdownContainer(
          content = Source.fromFile(file).mkString,
          lastModified = LocalDateTime.ofEpochSecond(file.lastModified(), 0, ZoneOffset.UTC)
        )
      } toSeq}) getOrElse Seq.empty
    }
  }
}
