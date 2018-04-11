package duesoldi.resources

import duesoldi.streams.InputStreams

import scala.util.Try

object Resources
{
  def loadBytes(path: String): Try[Array[Byte]] =
    Try {
      getClass.getClassLoader.getResourceAsStream(path)
    } flatMap { stream =>
      InputStreams.getBytes(stream)
    }
}
