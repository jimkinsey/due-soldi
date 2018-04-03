package sommelier.messaging

sealed trait Method

object Method
{
  case object GET extends Method
  case object HEAD extends Method
  case object POST extends Method
  case object PUT extends Method
  case object DELETE extends Method

  def apply(name: String): Method = {
    name match {
      case "GET" => GET
      case "HEAD" => HEAD
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
    }
  }
}