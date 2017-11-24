package object sommelier
{
  case class Context(request: Request, matcher: RequestMatcher)

  case class Request(method: Method, uri: String)

  case class Response(status: Int, body: Option[String] = None)
  {
    def apply(body: String): Response = copy(body = Some(body))
  }

  case class Route(matcher: RequestMatcher, handle: Routing.Handler)

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
}
