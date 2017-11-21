package duesoldi.collections

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

object MapEnhancements
{
  trait FieldTransformer[-IN,OUT]
  {
    def transform(in: IN): Option[OUT]
  }

  implicit class EnhancedMap[K,V](map: Map[K,V])
  {
    def field[T](key: K)(implicit vToT: FieldTransformer[V,T]): Option[T] = map.get(key).flatMap(vToT.transform)
  }

  implicit val stringField: FieldTransformer[Any,String] = str => Option(str.toString)
  implicit val dateTimeField: FieldTransformer[Any,ZonedDateTime] = {
    case str: String => Some(ZonedDateTime.parse(str, ISO_ZONED_DATE_TIME))
    case _ => None
  }
}
