package duesoldi.metrics.rendering

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import duesoldi.metrics.storage.AccessRecordStore.Access

object AccessCsv
{
  val header = "Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code,Request ID"

  def render(accessRecords: Seq[Access]): String = {
    val escaped = (string: String) => string.replace("\"", "\"\"").trim
    val quoted = (string: String) => s""""$string""""
    val csvFriendly = escaped andThen quoted

    val rows = accessRecords.map { case Access(time, path, referer, userAgent, duration, ip, country, statusCode, requestId) =>
      Seq(
        time.format(DateTimeFormatter.ISO_DATE_TIME),
        path,
        referer.getOrElse(""),
        userAgent.getOrElse(""),
        duration.toString,
        ip.getOrElse(""),
        country.getOrElse(""),
        statusCode.toString,
        requestId
      ).map(csvFriendly).mkString(",")
    }

    (header +: rows).mkString("\n")
  }

  def parse(csv: String): Either[ParseFailure, Seq[Access]] = Right { // FIXME error handling
    val unquoted = (string: String) => { // FIXME this is awful code
      val tmp = if (string.startsWith("\"")) string.tail else string
      if (tmp.endsWith("\"")) tmp.substring(0, tmp.length - 1) else tmp
    }
    val unescaped = (string: String) => string.replace("""\"""", "\"")
    csv.lines.toSeq.tail.map { row =>
      val columns = row.split(""","""").map(unquoted andThen unescaped)
      Access(
        time = ZonedDateTime.parse(columns(0), DateTimeFormatter.ISO_DATE_TIME),
        path = columns(1),
        referer = Option(columns(2)).filter(_.nonEmpty),
        userAgent = Option(columns(3)).filter(_.nonEmpty),
        duration = columns(4).toLong,
        clientIp = Option(columns(5)).filter(_.nonEmpty),
        country = Option(columns(6)).filter(_.nonEmpty),
        statusCode = columns(7).toInt,
        id = columns(8)
      )
    }
  }

  sealed trait ParseFailure
  object ParseFailure
  {

  }

}
