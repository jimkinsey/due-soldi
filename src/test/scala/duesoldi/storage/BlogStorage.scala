package duesoldi.storage

import java.io.{File, IOException, PrintWriter}
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.time.ZonedDateTime
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait BlogStorage {

  case class EntryBuilder(id: String = "id", content: String = "# Title", lastModified: ZonedDateTime = ZonedDateTime.now())

  implicit def tupleToBuilder(tuple: (String, String)): EntryBuilder = tuple match {
    case (id, content) => EntryBuilder(id, content)
  }

  implicit def tuple3ToBuilder(tuple: (String, String, String)): EntryBuilder = tuple match {
    case (time, id, content) => EntryBuilder(id, content, ZonedDateTime.parse(time))
  }

  def withBlogEntries[T <: Future[_]](entries: EntryBuilder*)(block: FilesystemMarkdownSource.Config => T)(implicit ec: ExecutionContext): T = {
    val config = new FilesystemMarkdownSource.Config {
      lazy val path = s"/tmp/blog/${UUID.randomUUID().toString.take(6)}"
    }
    entries foreach { case EntryBuilder(id, content, lastModified) =>
      val file = new File(s"${config.path}/$id.md")
      file.getParentFile.mkdirs()
      val writer = new PrintWriter(file)
      writer.write(content)
      writer.close()
      Files.setLastModifiedTime(file.toPath, FileTime.from(lastModified.toInstant))
    }
    val fut = block(config)
    fut.onComplete( _ => DeleteDir(new File(config.path).toPath))
    fut
  }

}

object DeleteDir {

  def apply(path: Path) = {
    Files.walkFileTree(path, new SimpleFileVisitor[Path](){
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, e: IOException): FileVisitResult = {
        handleException(e)
      }

      private def handleException(e: IOException) = {
        e.printStackTrace()
        FileVisitResult.TERMINATE
      }

      override def postVisitDirectory(dir: Path, e: IOException): FileVisitResult = {
        Option(e).map(handleException).getOrElse {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      }
    })
  }

}