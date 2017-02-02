package duesoldi.storage

import java.io.File

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

trait MarkdownSource {
  def document(id: String): Future[Option[String]]
  def documents: Future[Seq[(String, String)]]
}

object FilesystemMarkdownSource {
  trait Config {
    def path: String
  }
}

class FilesystemMarkdownSource(implicit config: FilesystemMarkdownSource.Config) extends MarkdownSource {
  override def document(id: String): Future[Option[String]] =
    Future successful Try(Source.fromFile(new File(s"${config.path}/$id.md")).mkString).toOption

  override def documents: Future[Seq[(String, String)]] = {
    Future successful {
      Try({new File(config.path).listFiles().filter(_.getName.endsWith("md")).map { file =>
        file.getName.dropRight(3) -> Source.fromFile(file).mkString
      } toSeq}) getOrElse Seq.empty
    }
  }
}
