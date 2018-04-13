package hammerspace.collections

object MapEnhancements
{
  implicit class EnhancedMap[K,V](map: Map[K,V])
  {
    def field[T](key: K)(implicit vToT: Coercion[V,T]): Option[T] = map.get(key).flatMap(vToT.coerce)
  }
  implicit class EnhancedStringKeyedMap[T](map: Map[String,T])
  {
    def lowKeys: Map[String,T] = map.map { case (key, value) => key.toLowerCase -> value }
  }
}
