package hammerspace.streams

import java.io.InputStream

import scala.util.Try

object InputStreams
{
  def getBytes(inputStream: InputStream): Try[Array[Byte]] = Try {
    val buffer = new java.io.ByteArrayOutputStream
    val data = new Array[Byte](16384)
    var nRead: Int = 0
    while ( {
      nRead != -1
    }) {
      buffer.write(data, 0, nRead)
      nRead = inputStream.read(data, 0, data.length)
    }
    buffer.flush()

    buffer.toByteArray
  }

  def toByteStream(inputStream: InputStream): Stream[Byte] = {
    toByteStream(inputStream, {})
  }

  def toByteStream(inputStream: InputStream, onClose: => Unit = {}): Stream[Byte] = {
    val bytes = new Array[Byte](1)
    val read = inputStream.read(bytes, 0, 1)
    if (read != -1) {
      bytes.head #:: toByteStream(inputStream, onClose)
    }
    else {
      inputStream.close()
      onClose
      Stream.empty
    }
  }
}
