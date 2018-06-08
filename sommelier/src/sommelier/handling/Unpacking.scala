package sommelier.handling

import java.time.ZonedDateTime

import sommelier.routing.SyncResult.{Accepted, Rejected}
import sommelier.routing.{AsyncResult, PathParams, Rejection, Result, SyncResult}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Try
import ratatoskr.RequestAccess._
import ratatoskr.ResponseBuilding._
import hammerspace.testing.StreamHelpers._
import ratatoskr.Response

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
    lazy val response = Response(400, body = s"Path var $name could not be unpacked".asByteStream("UTF-8"))
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
    lazy val response: Response = Response(500).content("Path is not wildcarded")
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
      body <- Option(context.request.body.asString).filter(_.nonEmpty) rejectWith { RequestHasNoBody }
      unpacked <- unpacker.unpack(body) rejectWith { BodyUnpackFailure }
    } yield {
      unpacked
    }
  }

  def header(name: String)(implicit context: Context): Result[String] = {
    context.request.headers.get(name).flatMap(_.headOption) rejectWith { HeaderNotFound(name) }
  }

  def headers(name: String)(implicit context: Context): Result[Seq[String]] = {
    context.request.headers.get(name) rejectWith { HeaderNotFound(name) }
  }

  def query[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Result[Seq[T]] = {
    context.request.queryParams.get(name) rejectWith { QueryParamNotFound(name) } map (_.flatMap(unpacker.unpack))
  }

  case class HeaderNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400).content(s"Header '$name' not found in request")
  }

  case class QueryParamNotFound(name: String) extends Rejection
  {
    val response: Response = Response(400).content(s"Query param '$name' not found in request")
  }

  case object RequestHasNoBody extends Rejection
  {
    val response: Response = Response(400).content("Request has no body")
  }

  case object BodyUnpackFailure extends Rejection
  {
    val response: Response = Response(500).content("Failed to unpack the body")
  }

  implicit val unpackInt: Unpacker[Int] = string => Try(string.toInt).toOption
  implicit val unpackLong: Unpacker[Long] = string => Try(string.toLong).toOption
  implicit val unpackString: Unpacker[String] = Some(_)
  implicit val unpackZonedDateTime: Unpacker[ZonedDateTime] = string => Try(ZonedDateTime.parse(string)).toOption

  implicit class OptionRejection[T](opt: Option[T])
  {
    def rejectWith(ifNone: => Rejection): SyncResult[T] =
      opt.fold[SyncResult[T]](Rejected(ifNone))(Accepted(_))
  }

  implicit class AsyncOptionRejection[T](fOpt: Future[Option[T]])
  {
    def rejectWith(ifNone: => Rejection)(implicit executionContext: ExecutionContext): AsyncResult[T] =
      AsyncResult(fOpt.map(_ rejectWith ifNone))
  }

  implicit class EitherRejection[L,R](either: Either[L,R])
  {
    def rejectWith(ifLeft: L => Rejection): SyncResult[R] =
      either.fold[SyncResult[R]](left => Rejected(ifLeft(left)), Accepted(_))
  }

  implicit class AsyncEitherRejection[L,R](fEither: Future[Either[L,R]])
  {
    def rejectWith(ifLeft: L => Rejection)(implicit executionContext: ExecutionContext): AsyncResult[R] =
      AsyncResult(fEither.map(_ rejectWith ifLeft))
  }

  implicit class FutureRejection[T](fT: Future[T])
  {
    def rejectWith(ifFailure: Throwable => Rejection)(implicit executionContext: ExecutionContext): AsyncResult[T] =
      AsyncResult(fT.map(Accepted(_)))
  }

  implicit class SeqResult[T](result: Result[Seq[T]])
  {
    def optional: Result[Seq[T]] = result.recover(_ => Seq.empty)
    def firstValue: Result[Option[T]] = result.map(_.headOption)
  }

  implicit class OptionResult[T](result: Result[Option[T]])
  {
    def defaultTo(ifEmpty: => T): Result[T] = result.map(_.getOrElse(ifEmpty))
  }

}

