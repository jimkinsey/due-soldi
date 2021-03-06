package sommelier.test

import sommelier.routing.PathParams
import sommelier.routing.PathParams.Failure.PathMatchFailure
import utest._

object PathParamsTests
extends TestSuite
{
  val tests = this
  {
    "Path params" - {
      "contains no params when the pattern has no params" - {
        val result = PathParams("/pattern/")("/pattern/")
        assert(result == Right(Map.empty))
      }
      "contains params when the pattern has params fulfilled by the path" - {
        val result = PathParams("/:id/foo/:id2")("/42/foo/baz")
        assert(result == Right(Map("id" -> "42", "id2" -> "baz")))
      }
      "fails if the path does not match" - {
        val result = PathParams("/:id/foo/:id2")("/42/foo")
        assert(result == Left(PathMatchFailure))
      }
      "contains the rest of the path where a wildcard matches" - {
        val result = PathParams("/prefix/*")("/prefix/the/long/tail")
        assert(result == Right(Map("*" -> "the/long/tail")))
      }
      "contains the rest of the path where a wildcard matches and the path has vars" - {
        val result = PathParams("/:var/*")("/42/remainder")
        assert(result == Right(Map("var" -> "42", "*" -> "remainder")))
      }
    }
  }
}

