package hammerspace.collections

import hammerspace.collections.StreamEnhancements.EnhancedStream
import utest._

object StreamEnhancementsTests
extends TestSuite {

  val tests = this {

    "splitting a stream by a splice" - {

      "the original stream and nil when the stream does not contain the slice " - {
        val res = Seq(1, 2, 3, 4, 5).toStream.splitAtSlice(Seq(6, 7).toStream)
        assert(res == (Seq(1, 2, 3, 4, 5).toStream, Stream.empty))
      }

      "the portion of the stream before the first appearance of the slice, and the portion after" - {
        val res = Seq(1, 2, 3, 4, 5).toStream.splitAtSlice(Seq(3, 4).toStream)
        assert(res == (Seq(1, 2).toStream, Seq(5).toStream))
      }

    }

    "separating by a slice" - {

      "when the slice is not present" - {
        val stream = (1 to 10).toStream
        val res = stream.separatedBy(Seq(11))
        assert(res == Stream(stream))
      }

      "when the slice is present once" - {
        val stream = (1 to 10).toStream
        val res = stream.separatedBy(Seq(5))
        assert(
          res(0) == (1 to 4).toStream,
          res(1) == (6 to 10).toStream
        )
      }

    }

    "removing a prefix" - {

      "leaves the original stream where it doesn't begin with the prefix" - {
        val res = (1 to 10).toStream removePrefix (2 to 5).toStream
        assert(res == (1 to 10).toStream)
      }

      "returns the stream with the prefix stripped" - {
        val res = (1 to 10).toStream removePrefix (1 to 3).toStream
        assert(res == (4 to 10).toStream)
      }

    }

    "taking the stream up to a slice" - {

      "leaves the original stream when it does not contain the slice" - {
        val res = (1 to 10).toStream takeUntil (11 to 20)
        assert(res == (1 to 10).toStream)
      }

      "returns the stream up to the slice when it does contain it" - {
        val res = (1 to 10).toStream takeUntil (5 to 6)
        assert(res == (1 to 4).toStream)
      }

    }



  }

}
