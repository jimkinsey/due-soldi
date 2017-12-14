package sommelier

import sommelier.PathParams.Failure.PathMatchFailure

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
      params <- PathParams(pattern)(path) rejectWith { _ => PathMatchFailure }
      value <- unpacker.unpack(params(name)) rejectWith { BadPathVar(name) }
    } yield {
      value
    }
  }

  case object NotAWildcardedPath extends Rejection
  {
    lazy val response: Response = Response(500)("Path is not wildcarded")
  }

  case object PathMatchFailure extends Rejection
  {
    lazy val response: Response = Response(500)
  }

  def remainingPath(implicit context: Context): Result[String] = {
    for {
      pattern <- context.matcher.path.map(_.pathPattern) rejectWith  { RouteHasNoPath }
      path = context.request.path
      params <- PathParams(pattern)(path) rejectWith { _ => PathMatchFailure }
      value <- params.get("*") rejectWith { NotAWildcardedPath }
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
  sealed trait Failure
  object Failure
  {
    case object PathMatchFailure extends Failure
  }

  def apply(pattern: String)(path: String): Either[Failure, Map[String,String]] = {
    val patternSegments = segments(pattern)
    def firstSegment(path: String): Option[String] = path.split('/').headOption.filter(_.nonEmpty)
    def remainingAfterFirstSegment(path: String): String = path.split('/').tail.mkString("/")
    def hasVars(patternSegments: Seq[String]): Boolean = patternSegments.exists(_.startsWith(":")) || patternSegments.contains("*")

    if (!hasVars(patternSegments)) {
      if (pattern != path) {
        return Left(Failure.PathMatchFailure)
      }
      else {
        return Right(Map.empty)
      }
    }

    val (result, _) = patternSegments.foldLeft[(Either[Failure, Map[String,String]], String)]((Right(Map.empty), path.dropWhile(_ == '/'))) {
      case ((Right(acc), remaining), segment) if segment.startsWith(":") && firstSegment(remaining).isDefined =>
        (Right(acc ++ Map(segment.drop(1) -> firstSegment(remaining).get)), remainingAfterFirstSegment(remaining))
      case ((Right(acc), remaining), "*") if remaining.dropWhile(_ == '/').nonEmpty =>
        (Right(acc ++ Map("*" -> remaining)), "")
      case ((acc @ Right(_), remaining), segment) if firstSegment(remaining).contains(segment) =>
        (acc, remainingAfterFirstSegment(remaining))
      case (success @ (Right(_), ""), "") =>
        success
      case (fail @ (Left(_), _), _) =>
        fail
      case ((Right(_), remaining), segment) =>
        (Left(Failure.PathMatchFailure), path)
    }

    result
  }

  def segments(path: String): Seq[String] =
    path
      .split('/').toSeq
      .filter(_.nonEmpty)

  lazy val PathVariable: Regex = """^:(.+)$""".r
}
