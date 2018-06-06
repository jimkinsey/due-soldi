package hammerspace.testing

object StreamHelpers
{
  implicit class ByteStreamHelper(stream: Stream[Byte])
  {
    def asString: String = new String(stream.toArray, "UTF-8")
  }
  implicit class StringHelper(string: String)
  {
    def asByteStream(charsetName: String): Stream[Byte] = string.getBytes(charsetName).toStream
  }
}
