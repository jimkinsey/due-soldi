package duesoldi.collections

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

trait Coercion[-IN,OUT]
{
  def coerce(in: IN): Option[OUT]
}

object StandardCoercions
{
  implicit val stringField: Coercion[Any,String] = str => Option(str.toString)
  implicit val dateTimeField: Coercion[Any,ZonedDateTime] = {
    case str: String => Some(ZonedDateTime.parse(str, ISO_ZONED_DATE_TIME))
    case _ => None
  }
  implicit val mapField: Coercion[Any,Map[String,Any]] = {
    case map: Map[_,_] => Some(map.asInstanceOf[Map[String,Any]])
    case _ => None
  }
}