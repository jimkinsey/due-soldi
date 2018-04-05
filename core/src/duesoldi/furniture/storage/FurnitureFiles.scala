package duesoldi.furniture.storage

import java.io._
import java.util

import duesoldi.furniture.{CurrentPathAndContent, Furniture, FurnitureLoadError}

import scala.util.Try

object FurnitureFiles
{
  val currentPathAndContent: CurrentPathAndContent = (unversionedPath) => {
    Resources.loadBytes(s"furniture/$unversionedPath")
      .toEither
      .left.map(_ => FurnitureLoadError)
      .map(bytes => Furniture(s"/furniture/${util.Arrays.hashCode(bytes)}/$unversionedPath", bytes))
  }
}

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