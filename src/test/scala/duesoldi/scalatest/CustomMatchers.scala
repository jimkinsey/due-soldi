package duesoldi.scalatest

import java.time.format.DateTimeFormatter

import org.scalatest.matchers.{BeMatcher, MatchResult}

import scala.util.Try

trait CustomMatchers {

  def parsableAs(format: DateTimeFormatter) = new BeMatcher[String] {
    def apply(in: String) = MatchResult(Try(format.parse(in)).isSuccess, s"[$in] is not parsable in the requested format", s"Succesfully parsed")
  }

  def aValidLong = new BeMatcher[String] {
    def apply(in: String) = MatchResult(Try(in.toLong).isSuccess, s"[$in] is not a valid Long value", s"Successfully parsed $in as a Long")
  }

}
