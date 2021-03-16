package duesoldi

package object images {
  type ImageResize = (Stream[Byte], Int) => Either[String, Stream[Byte]]
}
