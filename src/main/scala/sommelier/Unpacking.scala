package sommelier

import scala.language.postfixOps

object Unpacking
{
  def pathParam(name: String)(implicit context: Context): String = {
    PathParams(context.matcher.path.pathPattern)(context.request.uri)(name) // FIXME this URI stuff
  }
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
