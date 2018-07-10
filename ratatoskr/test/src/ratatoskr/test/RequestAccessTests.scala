package ratatoskr.test

import ratatoskr.Method.GET
import ratatoskr._
import ratatoskr.RequestAccess._
import utest._

import hammerspace.testing.StreamHelpers._

object RequestAccessTests
extends TestSuite
{
  val tests = this {
    "Accessing cookies" - {
      "returns None when there is no matching cookie header for the provided name" - {
        val request = Request(GET, "/", headers = EmptyHeaders)
        val cookie = request.cookie("sessionId")
        assert(cookie isEmpty)
      }
      "returns a cookie when there is a matching cookie header for the provided name" - {
        val request = Request(GET, "/", headers = Map("Cookie" -> Seq("sessionId=foo")))
        val cookie = request.cookie("sessionId")
        assert(cookie contains Cookie("sessionId", "foo"))
      }
    }
    "Getting the path" - {
      "returns the path from the request URI" - {
        val request = Request(GET, "http://host:8080/path/to/some.where")
        val path = request.path
        assert(path == "/path/to/some.where")
      }
    }
    "Getting a header" - {
      "returns none when the header is not present" - {
        val request = Request(GET, "/", headers = EmptyHeaders)
        val header = request.header("foo")
        assert(header isEmpty)
      }
      "returns the header value when present" - {
        val request = Request(GET, "/", headers = Map("foo" -> Seq("42")))
        val header = request.header("foo")
        assert(header contains Seq("42"))
      }
      "treats header names as case-insensitive" - {
        val request = Request(GET, "/", headers = Map("foo" -> Seq("42")))
        val header = request.header("fOo")
        assert(header contains Seq("42"))
      }
    }
    "Getting a form value" - {
      "returns an empty map when the request has no form values" - {
        val request = Request(GET, "/", body = EmptyBody)
        val formValues = request.formValues
        assert(formValues isEmpty)
      }
      "returns a map containing the form values when a simple, single form value is present" - {
        val request = Request(GET, "/", body = "name=Jim".asByteStream("UTF-8"))
        val formValues = request.formValues
        assert(formValues.headOption contains ("name", Seq("Jim")))
      }
      "returns a map containing the form values when a simple param with multiple values is present" - {
        val request = Request(GET, "/", body = "name=Jim&name=James".asByteStream("UTF-8"))
        val formValues = request.formValues
        assert(formValues.headOption contains ("name", Seq("Jim", "James")))
      }
      "supports escaping of spaces with a plus sign in values" - {
        val request = Request(GET, "/", body = "name=Jim+Kinsey".asByteStream("UTF-8"))
        val formValues = request.formValues
        assert(formValues.headOption contains ("name", Seq("Jim Kinsey")))
      }
      "supports escaping using standard URL encoding of values" - {
        val request = Request(GET, "/", body = "data=%7B%22foo%22%3A42%7D".asByteStream("UTF-8"))
        val formValues = request.formValues
        assert(formValues.headOption contains ("data", List("""{"foo":42}""")))
      }
      "supports multi-line values" - {
        val request = Request(GET, "/", body = "data=%7B%0A%20%20%22foo%22%3A%2042%0A%7D".asByteStream("UTF-8"))
        val formValues = request.formValues
        assert(formValues.headOption contains ("data", List(
          """{
            |  "foo": 42
            |}""".stripMargin)))
      }
    }
  }
}
