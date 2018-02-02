package duesoldi.furniture.storage

import java.io.File

import duesoldi.furniture.{CurrentUrlPath, FurnitureFile, FurnitureNotFound}

object FurnitureFiles
{
  def currentUrlPath(furnitureBasePath: String): CurrentUrlPath = (unversionedPath) =>
    file(furnitureBasePath)(unversionedPath)
      .map(file => s"/furniture/${file.lastModified()}/$unversionedPath" -> file)

  def file(furnitureBasePath: String): FurnitureFile = (unversionedPath) =>
    Option(new File(s"$furnitureBasePath/$unversionedPath"))
      .filter(_.exists())
      .toRight({ FurnitureNotFound(unversionedPath) })
}
