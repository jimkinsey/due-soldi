package cicerone

case class Request(method: String, url: String, body: Option[String] = None, headers: Headers = Map.empty)

case class RequestBuilder(method: String = "GET", url: String = "http://localhost", body: Option[String] = None, headers: Headers = Map.empty)
{
  def GET(url: String): RequestBuilder = copy(method = "GET", url = url)
  def POST(url: String): RequestBuilder = copy(method = "POST", url = url)
  def POST(url: String, body: String): RequestBuilder = copy(method = "POST", url = url, body = Some(body))
  def PUT(url: String, body: String): RequestBuilder = copy(method = "PUT", url = url, body = Some(body))
  def DELETE(url: String): RequestBuilder = copy(method = "DELETE", url = url)

  def header(header: (String, String)): RequestBuilder = copy(headers = headers ++ Map(header._1 -> Seq(header._2)))

  lazy val build: Request = Request(method, url, body, headers)
}
