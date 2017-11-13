package duesoldi.test.support.matchers

import java.time.format.DateTimeFormatter

import scala.util.Try

object CustomMatchers { 

  implicit class StringParsableAs(in: String) {
    def hasDateFormat(format: DateTimeFormatter): Boolean = Try(format.parse(in)).isSuccess
    def hasDateFormat(pattern: String): Boolean = Try(DateTimeFormatter.ofPattern(pattern).parse(in)).isSuccess
    def isAValidLong: Boolean = Try(in.toLong).isSuccess
  }

  implicit class EitherAssertions[L,R](either: Either[L,R]) {
    def isLeftOf(value: L): Boolean = either.left.toOption.contains(value)
    def isRightOf(value: R): Boolean = either.toOption.contains(value)
  }

  implicit class OptionalItemRequired[T](option: Option[T]) {
    def meetsRequirements(requirements: (T => Boolean)*): Boolean = {
      option.exists(t => requirements.forall(_ (t)))
    }
  }
}
