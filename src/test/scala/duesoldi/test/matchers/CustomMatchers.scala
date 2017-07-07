package duesoldi.test.matchers

import java.time.format.DateTimeFormatter

import scala.util.Try

object CustomMatchers { 

  implicit class StringParsableAs(in: String) {
    def hasDateFormat(format: DateTimeFormatter) = Try(format.parse(in)).isSuccess
    def isAValidLong = Try(in.toLong).isSuccess
  }

}
