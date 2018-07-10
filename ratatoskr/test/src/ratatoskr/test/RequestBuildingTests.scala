package ratatoskr.test

import hammerspace.testing.StreamHelpers._
import ratatoskr.Method.POST
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
  }
}
