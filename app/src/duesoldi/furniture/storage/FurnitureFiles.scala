package duesoldi.furniture.storage

import java.util

import duesoldi.furniture.{CurrentPathAndContent, Furniture, FurnitureLoadError}
import hammerspace.resources.Resources

object FurnitureFiles
{
  val currentPathAndContent: CurrentPathAndContent = (unversionedPath) => {
    Resources.loadBytes(this.getClass, s"furniture/$unversionedPath")
      .toEither
      .left.map(_ => FurnitureLoadError)
      .map(bytes => Furniture(s"/furniture/${util.Arrays.hashCode(bytes)}/$unversionedPath", bytes))
  }
}
