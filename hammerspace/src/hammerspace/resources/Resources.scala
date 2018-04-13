package hammerspace.resources

import hammerspace.streams.InputStreams

import scala.util.Try

object Resources
{
  def loadBytes(`class`: Class[_], path: String): Try[Array[Byte]] =
    Try {
      `class`.getClassLoader.getResourceAsStream(path)
    } flatMap { stream =>
      InputStreams.getBytes(stream)
    }
}
