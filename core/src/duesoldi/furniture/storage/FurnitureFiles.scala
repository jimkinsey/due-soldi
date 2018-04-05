package duesoldi.furniture.storage

import java.util

import duesoldi.furniture.{CurrentPathAndContent, Furniture, FurnitureLoadError}
import duesoldi.resources.Resources

object FurnitureFiles
{
  val currentPathAndContent: CurrentPathAndContent = (unversionedPath) => {
    Resources.loadBytes(s"furniture/$unversionedPath")
      .toEither
      .left.map(_ => FurnitureLoadError)
      .map(bytes => Furniture(s"/furniture/${util.Arrays.hashCode(bytes)}/$unversionedPath", bytes))
  }
}
