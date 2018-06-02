package sommelier.messaging

case class Request(
  method: Method,
  path: String,
  headers: Map[String, Seq[String]] = Map.empty,
  queryParams: Map[String, Seq[String]] = Map.empty,
  accept: Option[String] = None,
  body: Option[String] = None
)
{
  def apply(path: String) = copy(path = path)
  def header(header: (String, Seq[String])): Request = copy(headers = headers + header)
  def body(str: String): Request = copy(body = Some(str))
  def host(host: String): Request = header("Host" -> Seq(host))
}


