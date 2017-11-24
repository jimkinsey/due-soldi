package sommelier

import scala.language.postfixOps
import scala.util.Try

object Unpacking {

  trait Unpacker[T] {
    def unpack(string: String): Option[T]
  }

  case class BadPathVar(name: String) extends Rejection
  {
    lazy val response = Response(400, Some(s"Path var $name could not be unpacked"))
  }

  def pathParam[T](name: String)(implicit context: Context, unpacker: Unpacker[T]): Either[Rejection, T] = {
    val pattern = context.matcher.path.pathPattern
    val path = context.request.path
    unpacker.unpack(PathParams(pattern)(path)(name)).toRight({ BadPathVar(name) })
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
