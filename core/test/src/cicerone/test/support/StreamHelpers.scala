package cicerone.test.support

object StreamHelpers
{
  implicit class ByteStreamHelper(stream: Stream[Byte])
  {
    def asString: String = new String(stream.toArray, "UTF-8")
  }
}
