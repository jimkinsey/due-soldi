package sommelier.test

import ratatoskr.Method.POST
import ratatoskr.RequestBuilding._
import sommelier.handling.Unpacking.FormValueNotFound
import sommelier.handling.{Context, Unpacking}
import sommelier.routing.RequestMatcher
import sommelier.routing.SyncResult.{Accepted, Rejected}
import utest._

import scala.concurrent.ExecutionContext

object UnpackingTests
extends TestSuite
{
  implicit val executionContext: ExecutionContext = utest.framework.ExecutionContext.RunNow
  val tests: Tests = this
  {
    "Form values" - {
      "return a rejection when the form value is not specified" - {
        implicit val context: Context = Context(POST("/", ""), anyMatcher)
        val result = Unpacking.form[String]("name")
        assert(result == Rejected(FormValueNotFound("name")))
      }
      "return a result containing the values when specified" - {
        implicit val context: Context = Context(POST("/") formValue "name" -> "Charlotte", anyMatcher)
        val result = Unpacking.form[String]("name")
        assert(result == Accepted(Seq("Charlotte")))
      }
      "does not return values that cannot be unpacked" - {
        implicit val context: Context = Context(POST("/") formValue "name" -> "Charlotte", anyMatcher)
        val result = Unpacking.form[Int]("name")
        assert(result == Accepted(Seq()))
      }
    }

  }

  private lazy val anyMatcher = RequestMatcher()
}
