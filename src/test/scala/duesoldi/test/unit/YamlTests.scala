package duesoldi.test.unit

import duesoldi.test.support.matchers.CustomMatchers._
import duesoldi.yaml.Yaml
import utest._

object YamlTests
extends TestSuite
{
  val tests = this
  {
    "Parsing a YAML string to an object" - {
      "yields an empty map for an empty string" - {
        val emptyYaml = Yaml.obj("")
        assert(emptyYaml isRightOf Map.empty)
      }
      "handles a simple key value pair" - {
        val yaml = Yaml.obj("foo: bar")
        assert(yaml isRightOf Map("foo" -> "bar"))
      }
      "handles multiple key value pairs" - {
        val yaml = Yaml.obj(
          """foo: bar
            |fizz: buzz
          """.stripMargin
        )
        assert(yaml isRightOf Map("foo" -> "bar", "fizz" -> "buzz"))
      }
      "does not include fields with no value" - {
        val yaml = Yaml.obj("foo: ")
        assert(yaml isRightOf Map.empty)
      }
      "handles fields with multiline strings, preserving newlines" - {
        val yaml = Yaml.obj(
          """code: |
            |  10 PRINT "Hello"
            |  20 GOTO 10""".stripMargin)
        assert(yaml isRightOf Map("code" ->
          """10 PRINT "Hello"
            |20 GOTO 10""".stripMargin))
      }
      "allows blank lines between fields" - {
        val yaml = Yaml.obj(
          """a: 1
            |
            |b: 2
          """.stripMargin
        )
        assert(yaml isRightOf Map("a" -> "1", "b" -> "2"))
      }
    }
    "parsing a YAML string to an array" - {
      "yields an empty array if the string is empty" - {
        val empty = Yaml.arr("")
        assert(empty isRightOf Seq.empty)
      }
      "handles an array of objects" - {
        val objects = Yaml.arr(
          """ -
            |  obj: 1
            | -
            |  obj: 2
          """.stripMargin)
        assert(objects isRightOf Seq(Map("obj" -> "1"), Map("obj" -> "2")))
      }
    }
  }
}
