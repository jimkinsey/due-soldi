package duesoldi.test.unit

import duesoldi.collections.Coercion
import utest._
import duesoldi.collections.MapEnhancements._
import duesoldi.collections.StandardCoercions._

object MapEnhancementsTests
extends TestSuite
{
  val tests = this
  {
    "the field method" - {
      "returns None when the value is not set" - {
        assert(Map().field[String]("foo") == None)
      }
      "returns the value coerced to the type when possible" - {
        case object Bar
        implicit val toBar: Coercion[Any, Bar.type] = _ => Some(Bar)
        assert(Map("foo" -> "Bar").field[Bar.type]("foo") == Some(Bar))
      }
      "returns None when the coercion returns None" - {
        case object Foo
        implicit val toFoo: Coercion[Any, Foo.type] = _ => None
        assert(Map("foo" -> false).field[Foo.type]("foo") == None)
      }
    }
  }
}
