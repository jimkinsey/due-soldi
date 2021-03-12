package ratatoskr.test

import hammerspace.testing.StreamHelpers._
import ratatoskr.Method._
import ratatoskr.{Request}
import ratatoskr.RequestAccess.RequestAccessor
import ratatoskr.RequestBuilding._
import utest._

object RequestBuildingTests
extends TestSuite
{
  val tests = this {
    "Setting a form value" - {
      "applies it to the request body when the body is empty" - {
        val request: Request = POST("/", "").formValue("name" -> "Jim")
        val body = request.body.asString
        assert(body == "name=Jim")
      }
      "appends it to the request body with an ampersand when the body is non-empty" - {
        val request: Request = POST("/", "name=Jim").formValue("age" -> "39")
        val body = request.body.asString
        assert(body == "name=Jim&age=39")
      }
      "URL encodes values which need it" - {
        val request: Request = POST("/", "").formValue("data" -> """{"foo": 42}""")
        val body = request.body.asString
        assert(body == "data=%7B%22foo%22%3A+42%7D")
      }
    }

    "Setting up a multipart request" - {

      "initialises the content type if not already set" - {
        val request: Request = POST("/", "").multipart()
        assert(request.headers.get("Content-Type") exists (_.head startsWith "multipart/form-data; boundary="))
      }

    }

    "Setting a simple multipart form value" - {

      "prefixes the body with the boundary if the body is empty" - {
        val request: Request = POST("/", "").multipart().multipartFormValue("data" -> "42")
        val body = request.body.asString
        val boundary = request.multipartBoundary.get
        assert(body.lines.toList(0) == s"--$boundary")
      }

      "appends a Content-Disposition line containing the name, followed by a CRLF" - {
        val request = POST("/", "").multipart().multipartFormValue("data" -> "42")
        val body = request.body.asString
        assert(body contains "Content-Disposition: form-data; name=\"data\"\r\n")
      }

      "appends the data after the content disposition line" - {
        val request = POST("/", "").multipart().multipartFormValue("data" -> "42")
        val body = request.body.asString
        assert(body.lines.toList(2) == "42")
      }

      "appends the boundary after the data" - {
        val request = POST("/", "").multipart().multipartFormValue("data" -> "42")
        val body = request.body.asString
        assert(body.lines.toList(3) == s"--${request.multipartBoundary.get}")
      }

    }

    "Setting a multipart form value for a file" - {

      "prefixes the body with the boundary if the body is empty" - {
        val request: Request =
          POST("/", "")
            .multipart()
            .multipartFormValueFile(
              name = "cv",
              filename = "cv.md",
              contentType = "text/markdown",
              data = "# Curriculum Vitae".getBytes().toStream
            )
        val body = request.body.asString
        val boundary = request.multipartBoundary.get
        assert(body.lines.toList(0) == s"--$boundary")
      }

      "appends a Content-Disposition line containing the name and filename, followed by a CRLF" - {
        val request =
          POST("/", "")
            .multipart()
            .multipartFormValueFile(
              name = "cv",
              filename = "cv.md",
              contentType = "text/markdown",
              data = "# Curriculum Vitae".getBytes().toStream
            )
        val body = request.body.asString
        assert(body contains "Content-Disposition: form-data; name=\"cv\"; filename=\"cv.md\"\r\n")
      }

      "appends a Content-Type line containing the content type, followed by a CRLF" - {
        val request =
          POST("/", "")
            .multipart()
            .multipartFormValueFile(
              name = "cv",
              filename = "cv.md",
              contentType = "text/markdown",
              data = "# Curriculum Vitae".getBytes().toStream
            )
        val body = request.body.asString
        assert(body contains "Content-Type: text/markdown\r\n")
      }

      "appends the data after the content disposition line" - {
        val request =
          POST("/", "")
            .multipart()
            .multipartFormValueFile(
              name = "cv",
              filename = "cv.md",
              contentType = "text/markdown",
              data = "# Curriculum Vitae".getBytes().toStream
            )
        val body = request.body.asString
        assert(body.lines.toList(3) == "# Curriculum Vitae")
      }

      "appends the boundary after the data" - {
        val request =
          POST("/", "")
            .multipart()
            .multipartFormValueFile(
              name = "cv",
              filename = "cv.md",
              contentType = "text/markdown",
              data = "# Curriculum Vitae".getBytes().toStream
            )
        val body = request.body.asString
        assert(body.lines.toList.last == s"--${request.multipartBoundary.get}")
      }


    }

    "Setting a query" - {
      "appends the query string to the URL" - {
        val request: Request = GET("http://localhost/action").query(Map("foo" -> Seq("42")))
        val url = request.url
        assert(url == "http://localhost/action?foo=42")
      }
      "encodes query string values" - {
        val request: Request = GET("http://localhost/action").query(Map("foo" -> Seq("21 21")))
        val url = request.url
        assert(url == "http://localhost/action?foo=21+21")
      }
      "appends multiple parameters when there are many values" - {
        val request: Request = GET("http://localhost/action").query(Map("foo" -> Seq("6", "7")))
        val url = request.url
        assert(url == "http://localhost/action?foo=6&foo=7")
      }
      "supports multiple parameters" - {
        val request: Request = GET("http://localhost/action").query(Map("foo" -> Seq("42"), "bar" -> Seq("21")))
        val url = request.url
        assert(url == "http://localhost/action?foo=42&bar=21")
      }
    }
  }
}
