package duesoldi.storage

import java.io.{File, IOException, PrintWriter}
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.time.ZonedDateTime
import java.util.UUID

import duesoldi.{Env, Setup}

import scala.concurrent.{ExecutionContext, Future}

trait BlogStorage {
  implicit def executionContext: ExecutionContext

  case class EntryBuilder(id: String = "id", content: String = "# Title", lastModified: ZonedDateTime = ZonedDateTime.now())

  implicit def tupleToBuilder(tuple: (String, String)): EntryBuilder = tuple match {
    case (id, content) => EntryBuilder(id, content)
  }

  implicit def tuple3ToBuilder(tuple: (String, String, String)): EntryBuilder = tuple match {
    case (time, id, content) => EntryBuilder(id, content, ZonedDateTime.parse(time))
  }

  def blogEntries(entries: EntryBuilder*) = new Setup {
    lazy val path = s"/tmp/blog/${UUID.randomUUID().toString.take(6)}"

    override def setup: Future[Env] = {
      entries foreach { case EntryBuilder(id, content, lastModified) =>
        val file = new File(s"$path/$id.md")
        file.getParentFile.mkdirs()
        val writer = new PrintWriter(file)
        writer.write(content)
        writer.close()
        Files.setLastModifiedTime(file.toPath, FileTime.from(lastModified.toInstant))
      }
      Future.successful(Map("BLOG_STORE_PATH" -> path))
    }

    override def tearDown: Future[Unit] = {
      Future.successful(DeleteDir(new File(path).toPath))
    }

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