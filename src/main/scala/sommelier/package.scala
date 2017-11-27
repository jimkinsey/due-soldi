package object sommelier
{
  case class Context(request: Request, matcher: RequestMatcher)

  case class Request(
    method: Method,
    path: String,
    headers: Map[String, Seq[String]] = Map.empty,
    queryParams: Map[String, Seq[String]] = Map.empty,
    accept: Option[String] = None,
    body: Option[String] = None
  )

  case class Response(
    status: Int,
    body: Option[String] = None,
    contentType: Option[String] = None,
    location: Option[String] = None,
    wwwAuthenticate: Option[String] = None
  )
  {
    def apply(body: String): Response = copy(body = Some(body))
    def ContentType(contentType: String): Response = copy(contentType = Some(contentType))
    def Location(uri: String): Response = copy(location = Some(uri))
    def WWWAuthenticate(auth: String): Response = copy(wwwAuthenticate = Some(auth))
    def body(body: String): Response = this(body)
  }

  trait Rejection
  {
    def response: Response
  }
  object Rejection
  {
    def apply(r: Response) = new Rejection {
      override def response: Response = r
    }
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
