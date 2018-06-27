package duesoldi.metrics.rendering

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
}
