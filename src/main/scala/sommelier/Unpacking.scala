package sommelier

import scala.language.postfixOps
import scala.util.Try

object Unpacking {

  trait Unpacker[T] {
    def unpack(string: String): Option[T]
  }

  case object RouteHasNoPath extends Rejection
  {
    lazy val response = Response(500)
  }

  case class BadPathVar(name: String) extends Rejection
  {
    lazy val response = Response(400, Some(s"Path var $name could not be unpacked"))
  }

  def pathParam[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Either[Rejection, T] = {
    for {
      pattern <- context.matcher.path.map(_.pathPattern) toRight { RouteHasNoPath }
      path = context.request.path
      value <- unpacker.unpack(PathParams(pattern)(path)(name)) toRight { BadPathVar(name) }
    } yield {
      value
    }
  }

  def body[T](implicit context: Context, unpacker: Unpacker[T]): Either[Rejection, T] = {
    for {
      body <- context.request.body.toRight { RequestHasNoBody }
      unpacked <- unpacker.unpack(body).toRight { BodyUnpackFailure }
    } yield {
      unpacked
    }
  }

  def header(name: String)(implicit context: Context): Either[Rejection, Seq[String]] = {
    context.request.headers.get(name).toRight { HeaderNotFound(name) }
  }

  def query[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Either[Rejection, Seq[T]] = {
    context.request.queryParams.get(name).toRight { QueryParamNotFound(name) } .map(_.flatMap(unpacker.unpack))
  }

  case class HeaderNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400, Some(s"Header '$name' not found in request"))
  }

  case class QueryParamNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400, Some(s"Query param '$name' not found in request"))
  }

  case object RequestHasNoBody extends Rejection
  {
    val response: Response = Response(400, Some("Request has no body"))
  }

  case object BodyUnpackFailure extends Rejection
  {
    val response: Response = Response(500, Some("Failed to unpack the body"))
  }

  implicit val unpackInt: Unpacker[Int] = string => Try(string.toInt).toOption
  implicit val unpackString: Unpacker[String] = Some(_)
}

object PathParams
{
  def apply(pattern: String)(path: String): Map[String,String] = {
    segments(pattern) zip segments(path) collect {
      case (PathVariable(key), value) => key -> value
    } toMap
  }

  def segments(path: String) = path.split('/')

  lazy val PathVariable = """^:(.+)$""".r
}
