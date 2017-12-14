package sommelier.test

import sommelier.{PathMatcher, ResourceNotFound}
import utest._

import scala.language.postfixOps

object PathMatcherTests
extends TestSuite
{
  val tests = this
  {
    "A path matcher" - {
      "rejects when a pattern with no vars is matched against a different path" - {
        val rejection = PathMatcher("/path/to/content").rejects("/another/path")
        assert(rejection contains ResourceNotFound)
      }
      "does not reject when two identical literal paths are compared" - {
        val rejection = PathMatcher("/path").rejects("/path")
        assert(rejection isEmpty)
      }
      "rejects when a pattern with vars is matched against a non-matching path" - {
        val rejection = PathMatcher("/thing/:id").rejects("/nothing/id")
        assert(rejection contains ResourceNotFound)
      }
      "does not reject when a pattern with vars is matched against a path filling those vars" - {
        val rejection = PathMatcher("/thing/:id/:sub-id").rejects("/thing/123/456")
        assert(rejection isEmpty)
      }
      "does not reject when the pattern is wildcarded and the prefix matches" - {
        val rejection = PathMatcher("/en/*").rejects("/en/thing")
        assert(rejection isEmpty)
      }
      "does not reject when the pattern is wildcarded and contains variables" - {
        val rejection = PathMatcher("/:id/*").rejects("/123/fofofofof")
        assert(rejection isEmpty)
      }
      "rejects when the pattern is wildcarded and is matched against something with no suffix" - {
        val rejection = PathMatcher("/foo/*").rejects("/foo/")
        assert(rejection contains ResourceNotFound)
      }
      "rejects when the pattern has a trailing var compared to the path" - {
        val rejection = PathMatcher("/foo/:id").rejects("/foo")
        assert(rejection contains ResourceNotFound)
      }
    }
  }
}
