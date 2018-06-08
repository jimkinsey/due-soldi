package hammerspace.uri

case class URI(
  scheme: String,
  authority: String,
  path: String,
  queryString: Option[String] = None,
  fragment: Option[String] = None
)
{
  def addParam(param: (String, String)): URI = copy(queryString =
    queryString
      .map(_ + "&")
      .orElse(Some(""))
      .map(_ + s"${param._1}=${param._2}"))

  def withPath(path: String) = copy(path = path)

  lazy val format = {
    s"$scheme://$authority$path${queryString.map(q => s"?$q").getOrElse("")}${fragment.map(f => s"#$f").getOrElse("")}"
  }
}

object URI
{
  def parse(uri: String): URI = uri match {
    case regex(_, scheme, _, authority, path, _, queryString, _, fragment) => URI(scheme, authority, path, Option(queryString), Option(fragment))
  }

  private lazy val regex = """^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?""".r
}
