package duesoldi.test.unit

import duesoldi.json.CirceJson.toMap
import utest._

object CirceJsonTests
extends TestSuite
{
  import io.circe.{JsonObject, parser}

  val tests = this
  {
    "toMap" - {
      "turns a Circe JSON object into a Map" - {
        withJsonObject("""{}""") { obj =>
          assert(toMap(obj) == Map.empty)
        }
      }
      "turns string values into strings" - {
        withJsonObject("""{ "str": "foo" }""") { obj =>
          assert(toMap(obj) == Map("str" -> "foo"))
        }
      }
      "turns int values into ints" - {
        withJsonObject("""{ "int": 42 }""") { obj =>
          assert(toMap(obj) == Map("int" -> 42))
        }
      }
      "turns float values into floats" - {
        withJsonObject("""{ "float": 41.997 }""") { obj =>
          assert(toMap(obj) == Map("float" -> 41.997))
        }
      }
      "turns boolean values into booleans" - {
        withJsonObject("""{ "bool": true }""") { obj =>
          assert(toMap(obj) == Map("bool" -> true))
        }
      }
      "recursively handles maps" - {
        withJsonObject("""{ "obj": { "key": "value" } }""") { obj =>
          assert(toMap(obj) == Map("obj" -> Map("key" -> "value")))
        }
      }
      "recursively handles arrays" - {
        withJsonObject("""{ "arr": [ 4, 8, 15, 16 ] }""") { obj =>
          assert(toMap(obj) == Map("arr" -> Seq(4, 8, 15, 16)))
        }
      }
      "does not include null values" - {
        withJsonObject("""{ "unspecified": null }""") { obj =>
          assert(toMap(obj) == Map.empty)
        }
      }
    }
  }

  def withJsonObject[T](jsonString: String)(block: JsonObject => T): Unit = {
    for {
      json <- parser.parse(jsonString).left.map(failure => require(false, s"[$jsonString] could not be parsed - $failure"))
      obj <- json.asObject.toRight({ require(false, s"[$jsonString] is not an object") })
    } yield {
      block(obj)
    }
  }
}
