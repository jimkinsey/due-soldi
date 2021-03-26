package duesoldi

package object furniture
{
  case object FurnitureLoadError
  case class Furniture(path: String, content: Array[Byte])
  case class FurnitureCacheConfig(durationHours: Option[Int])

  type CurrentPathAndContent = (String) => Either[FurnitureLoadError.type, Furniture]
}
