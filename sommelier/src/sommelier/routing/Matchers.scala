package sommelier.routing

import java.util.Base64

import ratatoskr.Method
import sommelier.messaging.{Request, Response}

import scala.util.matching.Regex

trait Rejects[T]
{
  def rejects(t: T): Option[Rejection]
}

case object MethodNotAllowed extends Rejection
{
  val response: Response = Response(405)
}

case class MethodMatcher(methods: Method*) extends Rejects[Method]
{
  def rejects(method: Method): Option[Rejection] = if (methods.contains(method)) None else Some(MethodNotAllowed)
}

case object ResourceNotFound extends Rejection
{
  val response: Response = Response(404)
}

case class PathMatcher(pathPattern: String) extends Rejects[String]
{
  def rejects(path: String): Option[Rejection] = {
    val result = PathParams(pathPattern)(path)
    result.swap.map(_ => ResourceNotFound).toOption
  }
}

case class RequestMatcher(
  method: Option[MethodMatcher] = None,
  path: Option[PathMatcher] = None,
  accept: Option[AcceptMatcher] = None,
  authorization: Option[AuthorizationMatcher] = None,
  host: Option[HostMatcher] = None
) extends Rejects[Request]
{
  def apply(path: String): RequestMatcher = {
    copy(path = Some(PathMatcher(path)))
  }

  def rejects(request: Request): Option[Rejection] = {
    path.flatMap(_.rejects(request.path)) orElse
    method.flatMap(_.rejects(request.method)) orElse
    accept.flatMap(_.rejects(request.accept.getOrElse(""))) orElse // FIXME
    authorization.flatMap(_.rejects(request)) orElse
    host.flatMap(_.rejects(request.headers("Host").head)) // FIXME
  }

  def Accept(contentType: String): RequestMatcher = {
    copy(accept = Some(AcceptMatcher(contentType)))
  }

  def Authorization(authorization: AuthorizationMatcher): RequestMatcher = {
    copy(authorization = Some(authorization))
  }

  def Host(host: String): RequestMatcher = {
    copy(host = Some(HostMatcher(host)))
  }

  override def toString: String =
    s"""${method.map(_.methods.mkString(",")).getOrElse("[Any Method]")} ${path.map(_.pathPattern).getOrElse("*")}
       |Accept: ${accept.map(_.contentType).getOrElse("*/*")}
       |Authorization: ${authorization.getOrElse("n/a")}
       |Host: ${host.map(_.host).getOrElse("[Any Host]")}""".stripMargin
}

case object Unacceptable extends Rejection
{
  val response: Response = Response(406)
}

case class AcceptMatcher(contentType: String) extends Rejects[String]
{
  def rejects(reqContentType: String): Option[Rejection] = {
    if (contentType == reqContentType) None else Some(Unacceptable)
  }
}

sealed trait AuthorizationFailed extends Rejection
object AuthorizationFailed {
  case class Unauthorized(realm: String) extends AuthorizationFailed
  {
    val response: Response = Response(401) WWWAuthenticate s"""Basic realm="$realm""""
  }
  case object Forbidden extends AuthorizationFailed
  {
    val response: Response = Response(403)
  }
}

object AuthorizationMatcher
{
  implicit class WithFallback(initial: AuthorizationMatcher) {
    def or(next: AuthorizationMatcher): AuthorizationMatcher = {
      request: Request => {
        initial.rejects(request) match {
          case None => None
          case _ => next.rejects(request)
        }
      }
    }
  }
}
trait AuthorizationMatcher extends Rejects[Request]
{
  def rejects(request: Request): Option[Rejection]
}
object Basic
{
  val Creds: Regex = s"""^Basic (.+)""".r
}
case class Basic(username: String, password: String, realm: String) extends AuthorizationMatcher
{
  def rejects(request: Request): Option[Rejection] = {
    request.headers.get("Authorization").flatMap(_.headOption) match {
      case None => Some(AuthorizationFailed.Unauthorized(realm))
      case Some(Basic.Creds(requestCreds)) if requestCreds == encoded => None
      case _ => Some(AuthorizationFailed.Forbidden)
    }
  }

  lazy val encoded: String = Base64.getEncoder.encodeToString(s"$username:$password".getBytes("UTF-8"))

  override def toString: String = s"Basic ***"
}

case class HostMatcher(host: String) extends Rejects[String]
{
  override def rejects(in: String): Option[Rejection] = {
    if (host == in) {
      None
    } else {
      Some(BadRequest)
    }
  }
}

case object BadRequest extends Rejection {
  override def response: Response = Response(400)
}