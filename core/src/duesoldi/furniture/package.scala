package duesoldi

import java.io.File

package object furniture
{
  case class FurnitureNotFound(path: String)

  type CurrentUrlPath = (String) => Either[FurnitureNotFound, (String, Array[Byte])]
//  type FurnitureFile = (String) => Either[FurnitureNotFound, File]
}
