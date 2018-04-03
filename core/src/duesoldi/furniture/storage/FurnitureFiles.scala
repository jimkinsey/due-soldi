package duesoldi.furniture.storage

import java.io._
import java.util

import duesoldi.furniture.{CurrentUrlPath, FurnitureNotFound}

object FurnitureFiles
{
  def currentUrlPath(furnitureBasePath: String): CurrentUrlPath = (unversionedPath) => {
    val is: InputStream = getClass.getClassLoader.getResourceAsStream(s"furniture/$unversionedPath")

    import java.io.ByteArrayOutputStream
    val buffer = new ByteArrayOutputStream

    var nRead: Int = 0
    val data = new Array[Byte](16384)

    while ( {
      nRead != -1
    }) {
      buffer.write(data, 0, nRead)
      nRead = is.read(data, 0, data.length)
    }

    buffer.flush()

    val bytes = buffer.toByteArray
    Right[FurnitureNotFound, (String, Array[Byte])](s"/furniture/${util.Arrays.hashCode(bytes)}/$unversionedPath" -> bytes)
  }
}
