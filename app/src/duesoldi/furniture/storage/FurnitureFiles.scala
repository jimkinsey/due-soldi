package duesoldi.furniture.storage

import duesoldi.furniture.{CurrentPathAndContent, Furniture, FurnitureLoadError}
import hammerspace.resources.Resources

import java.io.File
import java.nio.file.Files
import java.util
import scala.util.Try

object FurnitureFiles
{
  val fromResources: CurrentPathAndContent = (unversionedPath) => {
    Resources.loadBytes(this.getClass, s"furniture/$unversionedPath")
      .toEither
      .left.map(_ => FurnitureLoadError)
      .map(bytes => Furniture(s"/furniture/${util.Arrays.hashCode(bytes)}/$unversionedPath", bytes))
  }

  val fromProject: CurrentPathAndContent = (unversionedPath) => {
    Try(Files.readAllBytes(new File(s"./app/resources/furniture/$unversionedPath").toPath))
      .toEither
      .left.map(_ => FurnitureLoadError)
      .map( bytes => Furniture(s"/furniture/0/$unversionedPath", bytes) )
  }
}
