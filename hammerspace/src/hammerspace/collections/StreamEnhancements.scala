package hammerspace.collections

import scala.collection.GenSeq

object StreamEnhancements {

  implicit class EnhancedStream[T](stream: Stream[T]) {

    def splitAtSlice(slice: GenSeq[T]): (Stream[T], Stream[T]) = {
      stream.indexOfSlice(slice) match {
        case -1 => (stream, Stream.empty)
        case i => (stream.slice(0, i), stream.drop(i + slice.length))
      }
    }

    def removePrefix(prefix: GenSeq[T]): Stream[T] = {
      if (stream.startsWith(prefix))
        stream.drop(prefix.length)
      else
        stream
    }

    def separatedBy(slice: GenSeq[T]): Stream[Stream[T]] = {
      stream.indexOfSlice(slice) match {
        case -1 => Stream(stream)
        case i => stream.take(i) #:: stream.drop(i + slice.length).separatedBy(slice)
      }
    }

    def takeUntil(slice: GenSeq[T]): Stream[T] = {
      stream.indexOfSlice(slice) match {
        case -1 => stream
        case i => stream.slice(0, i)
      }
    }

  }

}
