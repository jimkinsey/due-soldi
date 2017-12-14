package sommelier

import scala.language.postfixOps
import scala.util.Try
import scala.util.matching.Regex

object Unpacking {
  import Result._

  trait Unpacker[T] {
    def unpack(string: String): Option[T]
  }

  case object RouteHasNoPath extends Rejection
  {
    lazy val response = Response(500)
  }

  case class BadPathVar(name: String) extends Rejection
  {
    lazy val response = Response(400)(s"Path var $name could not be unpacked")
  }

  def pathParam[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Result[T] = {
    for {
      pattern <- context.matcher.path.map(_.pathPattern) rejectWith  { RouteHasNoPath }
      path = context.request.path
      value <- unpacker.unpack(PathParams(pattern)(path)(name)) rejectWith { BadPathVar(name) }
    } yield {
      value
    }
  }

  case object NotAWildcardedPath extends Rejection
  {
    lazy val response: Response = Response(500)("Path is not wildcarded")
  }

  def remainingPath(implicit context: Context): Result[String] = {
    for {
      pattern <- context.matcher.path.map(_.pathPattern) rejectWith  { RouteHasNoPath }
      path = context.request.path
      value <- PathParams(pattern)(path).get("*") rejectWith { NotAWildcardedPath }
    } yield {
      value
    }
  }

  def body[T](implicit context: Context, unpacker: Unpacker[T]): Result[T] = {
    for {
      body <- context.request.body rejectWith { RequestHasNoBody }
      unpacked <- unpacker.unpack(body) rejectWith { BodyUnpackFailure }
    } yield {
      unpacked
    }
  }

  def header(name: String)(implicit context: Context): Result[Seq[String]] = {
    context.request.headers.get(name) rejectWith { HeaderNotFound(name) }
  }

  def query[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Result[Seq[T]] = {
    context.request.queryParams.get(name) rejectWith { QueryParamNotFound(name) } map (_.flatMap(unpacker.unpack))
  }

  case class HeaderNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400)(s"Header '$name' not found in request")
  }

  case class QueryParamNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400)(s"Query param '$name' not found in request")
  }

  case object RequestHasNoBody extends Rejection
  {
    val response: Response = Response(400)("Request has no body")
  }

  case object BodyUnpackFailure extends Rejection
  {
    val response: Response = Response(500)("Failed to unpack the body")
  }

  implicit val unpackInt: Unpacker[Int] = string => Try(string.toInt).toOption
  implicit val unpackLong: Unpacker[Long] = string => Try(string.toLong).toOption
  implicit val unpackString: Unpacker[String] = Some(_)
}

object PathParams
{
  def apply(pattern: String)(path: String): Map[String,String] = {
    val patternSegments = segments(pattern)
    val pathSegments = segments(path)
    val patternSegmentsUpToWildcard = patternSegments.takeWhile(_ != "*")
    val pathSegmentsUpToWildcard = pathSegments.take(patternSegmentsUpToWildcard.length)
    val pathSegmentsIncludingRemainder = pathSegmentsUpToWildcard :+ pathSegments.drop(patternSegmentsUpToWildcard.length).mkString("/")
    patternSegments zip pathSegmentsIncludingRemainder collect {
      case (PathVariable(key), value) if value.nonEmpty => key -> value
      case wildcardPart @ ("*", remainder) => wildcardPart
    } toMap
  }

  def segments(path: String): Array[String] = path.split('/')

  lazy val PathVariable: Regex = """^:(.+)$""".r
}
