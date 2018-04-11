package duesoldi.test.unit

import java.io.ByteArrayInputStream

import duesoldi.streams.InputStreams

import utest._

object InputStreamsTests
extends TestSuite
{
  val tests = this {
    "Converting an input stream to a byte stream" - {
      "returns an empty stream for an empty input stream" - {
        val stream = InputStreams.toByteStream(new ByteArrayInputStream(Array.emptyByteArray))
        assert(stream.isEmpty)
      }
      "returns a stream with the same first byte as the input stream" - {
        val bytes = "A".getBytes
        val stream = InputStreams.toByteStream(new ByteArrayInputStream(bytes))
        assert(stream.head == bytes.head)
      }
      "returns a stream where the next byte is the next byte of the input stream" - {
        val bytes = "ABC".getBytes
        val stream = InputStreams.toByteStream(new ByteArrayInputStream(bytes))
        assert(stream.tail.head == bytes.tail.head)
      }
      "closes the input stream when the last element of the stream has been read" - {
        var closed = false
        val inputStream = new ByteArrayInputStream("ABC".getBytes) {
          override def close(): Unit = {
            super.close()
            closed = true
          }
        }
        val stream = InputStreams.toByteStream(inputStream)
        stream.toList
        assert(closed)
      }
      "invokes the provided onClose handler when the input stream is exhausted" - {
        var handled = false
        val inputStream = new ByteArrayInputStream("ABC".getBytes)
        val stream = InputStreams.toByteStream(inputStream, onClose = { handled = true })
        stream.toList
        assert(handled)
      }
    }
  }
}
