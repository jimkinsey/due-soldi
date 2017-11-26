package sommelier

case class MethodMatcher(methods: Method*)
{
  def matches(method: Method): Boolean = methods.contains(method)
}

case class PathMatcher(pathPattern: String)
{
  def matches(path: String): Boolean = {
    val parts: Array[String] = path.split('/')
    val patternParts: Array[String] = pathPattern.split('/')

    if (parts.length != patternParts.length) return false

    parts.zip(patternParts).foldLeft(true) {
      case (acc, (part, pattern)) if part == pattern => acc
      case (acc, (_, pattern)) if pattern.startsWith(":") => acc
      case _ => false
    }
  }
}

case class RequestMatcher(
  method: MethodMatcher = MethodMatcher(Method.GET),
  path: PathMatcher = PathMatcher("/"),
  accepts: Option[AcceptsMatcher] = None
)
{
  def apply(path: String): RequestMatcher = {
    copy(path = PathMatcher(path))
  }

  def matches(request: Request): Boolean = {
    method.matches(request.method) &&
      path.matches(request.path) &&
      (accepts.isEmpty || accepts.exists(_.matches(request.accept.getOrElse("")))) // FIXME
  }

  def Accept(contentType: String): RequestMatcher = {
    copy(accepts = Some(AcceptsMatcher(contentType)))
  }
}

case class AcceptsMatcher(contentType: String)
{
  def matches(reqContentType: String): Boolean = {
    contentType == reqContentType
  }
}