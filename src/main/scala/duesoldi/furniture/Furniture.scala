package duesoldi.furniture

import java.io.File

object Furniture
{
  def currentPath(furnitureBasePath: String): CurrentFurniturePath = (unversionedPath) =>
    furnitureFile(furnitureBasePath)(unversionedPath)
      .map(file => s"/${file.lastModified()}/$unversionedPath" -> file)

  def furnitureFile(furnitureBasePath: String): FurnitureFile = (unversionedPath) =>
    Option(new File(s"$furnitureBasePath/$unversionedPath"))
      .filter(_.exists())
      .toRight({ FurnitureNotFound(unversionedPath) })
}
