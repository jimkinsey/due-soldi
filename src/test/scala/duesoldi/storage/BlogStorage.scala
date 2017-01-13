package duesoldi.storage

import java.io.{File, PrintWriter}

/**
  * Created by jimkinsey on 16/01/17.
  */
trait BlogStorage {

  def withBlogEntries[T](entries: (String, String)*)(block: => T): T = {
    entries foreach { case (id, content) =>
      val file = new File(s"/tmp/blog/$id.md")
      file.getParentFile.mkdirs()
      val writer = new PrintWriter(file)
      writer.write(content)
      writer.close()
    }
    block
  }

}
