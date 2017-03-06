package duesoldi.filesystem

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

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
