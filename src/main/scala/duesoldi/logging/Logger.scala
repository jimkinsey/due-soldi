package duesoldi.logging

import java.io.PrintStream
import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}

class Logger(name: String, loggingEnabled: Boolean = true) {

  def info(message: => String) { log(message, System.out) }
  def error(message: => String) { log(message, System.err) }

  private def log(message: String, stream: PrintStream) = {
    if (loggingEnabled) {
      stream.println(s"${ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)} [$name] $message")
    }
  }
}
