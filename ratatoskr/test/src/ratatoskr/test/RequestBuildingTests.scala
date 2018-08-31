package ratatoskr.test

import hammerspace.testing.StreamHelpers._
import ratatoskr.Method._
import ratatoskr.Request
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
