package duesoldi.resources

import java.io.InputStream

import scala.util.Try

object Resources
{
  def loadBytes(path: String): Try[Array[Byte]] = Try {
    val is: InputStream = getClass.getClassLoader.getResourceAsStream(path)

    val buffer = new java.io.ByteArrayOutputStream
    val data = new Array[Byte](16384)
    var nRead: Int = 0
    while ( {
      nRead != -1
    }) {
      buffer.write(data, 0, nRead)
      nRead = is.read(data, 0, data.length)
    }
    buffer.flush()

    buffer.toByteArray
  }
}
