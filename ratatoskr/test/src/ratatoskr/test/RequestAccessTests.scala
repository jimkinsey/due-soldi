package ratatoskr.test

import hammerspace.testing.StreamHelpers._
import ratatoskr.Method.{GET, POST}
import ratatoskr.RequestAccess._
import ratatoskr._
import utest._

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
    "Multi-part form values" - {

      "is empty when the request is not multi-part" - {
        val request = Request(
          method = POST,
          url = "/",
          body = "".asByteStream("UTF-8")
        )
        val formValues = request.multipartFormValues
        assert(formValues isEmpty)
      }

      "is empty when no boundary is specified in the content type header" - {
        val request = Request(
          method = POST,
          url = "/",
          body = "".asByteStream("UTF-8"),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data")
          ),
        )
        val formValues = request.multipartFormValues
        assert(formValues isEmpty)
      }

      "is empty when there is no Content-Disposition data" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |blah[CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(formValues isEmpty)
      }

      "is empty when there is no name specified in the Content-Disposition data" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data[CR]
              |
              |value[CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(formValues isEmpty)
      }

      "gives an empty stream when there is no value data in the section" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key"[CR]
              |
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(formValues.find(_.name == "key") exists(_.data.isEmpty))
      }

      "includes the value for the key specified in the section's content-disposition" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key"[CR]
              |
              |value[CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(
          formValues.find(_.name == "key") exists (_.data.asString == "value")
        )
      }

      "includes the value for the key specified in the section's content-disposition where there are multiple sections" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key1"[CR]
              |
              |value1[CR]
              |----b
              |Content-Disposition: form-data; name="key2"[CR]
              |
              |value2[CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(
          formValues.find(_.name == "key1") exists(_.data.asString == "value1"),
          formValues.find(_.name == "key2") exists(_.data.asString == "value2")
        )
      }

      "includes the content-type, when specified" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key"[CR]
              |Content-Type: text/plain[CR]
              |
              |Hello, world![CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(
          formValues.find(_.name == "key") exists (_.contentType contains "text/plain")
        )
      }

      "includes the filename, when specified in the content disposition" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key"; filename="greeting.txt"[CR]
              |Content-Type: text/plain[CR]
              |Hello, world![CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        assert(
          formValues.find(_.name == "key") exists (_.filename contains "greeting.txt")
        )
      }

      "includes the file data, when a file name and content type are specified" - {
        val request = Request(
          method = POST,
          url = "/",
          body = bodyWithCarriageReturns(
            """
              |----b
              |Content-Disposition: form-data; name="key"; filename="greeting.txt"[CR]
              |Content-Type: text/plain[CR]
              |Hello, world![CR]
              |----b""".stripMargin),
          headers = Map(
            "Content-Type" -> Seq("multipart/form-data; boundary=--b")
          ),
        )
        val formValues = request.multipartFormValues
        val textFileContent = formValues.find(_.name == "key").map(_.data.asString)
        assert(
          textFileContent contains "Hello, world!"
        )
      }

    }
  }

  private def bodyWithCarriageReturns(string: String): Stream[Byte] = {
    string.replace("[CR]", "\r").asByteStream("UTF-8")
  }
}
