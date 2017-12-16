import sommelier.Middleware.{Incoming, Outgoing}
import sommelier.routing.{RequestMatcher, Routing}

import scala.concurrent.duration.Duration

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
  {
    def header(header: (String, Seq[String])): Request = copy(headers = headers + header)
    def body(str: String): Request = copy(body = Some(str))
  }

  case class Response(
    status: Int,
    body: Option[Array[Byte]] = None,
    headers: Seq[(String, String)] = Seq.empty
  )
  {
    def apply(body: String): Response = this(body.getBytes("UTF-8"))
    def apply(body: Array[Byte]): Response = copy(body = Some(body))
    def ContentType(contentType: String): Response = header("Content-Type" -> contentType)
    def Location(uri: String): Response = header("Location" -> uri)
    def WWWAuthenticate(auth: String): Response = header("WWW-Authenticate" -> auth)
    def body(body: String): Response = this(body)
    def body(body: Array[Byte]): Response = this(body)
    def header(header: (String, String)): Response = copy(headers = headers :+ header)
    def body[T](implicit marshal: Array[Byte] => T): Option[T] = body map marshal

    override def equals(obj: scala.Any): Boolean = {
      def bodiesAreEqual(a: Option[Array[Byte]], b: Option[Array[Byte]]) = (a, b) match {
        case (Some(aBytes), Some(bBytes)) => java.util.Arrays.equals(aBytes, bBytes)
        case (None, None) => true
        case _ => false
      }

      obj match {
        case oth: Response =>
          oth.status == status && bodiesAreEqual(oth.body, body) && oth.headers == headers
        case _ => false
      }
    }

    override def toString: String = {
      s"""$status
         |${headers.map { case (key, value) => s"$key: $value" } mkString "\n"}
         |${body.map(bytes => new String(bytes, "utf-8")).getOrElse("")}""".stripMargin
    }
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

  sealed trait Middleware
  object Middleware
  {
    type IncomingHandler = Request => Result[Request]
    type OutgoingHandler = (Request, Response) => Result[Response]
    case class Incoming(matcher: RequestMatcher, handle: IncomingHandler) extends Middleware
    case class Outgoing(matcher: RequestMatcher, handle: OutgoingHandler) extends Middleware
  }

  implicit class MiddlewareMaker(matcher: RequestMatcher)
  {
    def incoming(handle: Request => Result[Request]): Middleware = Incoming(matcher, handle)
    def outgoing(handle: (Request, Response) => Result[Response]): Middleware = Outgoing(matcher, handle)
  }

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
