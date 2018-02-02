package duesoldi

import java.io.File

package object furniture
{
  case class FurnitureNotFound(path: String)

  type CurrentUrlPath = (String) => Either[FurnitureNotFound, (String, File)]
  type FurnitureFile = (String) => Either[FurnitureNotFound, File]
}
