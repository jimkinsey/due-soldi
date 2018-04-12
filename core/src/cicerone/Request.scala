package cicerone

case class Request(method: String, url: String, body: Option[String] = None)

case class RequestBuilder(method: String = "GET", url: String = "http://localhost", body: Option[String] = None)
{
  def GET(url: String): RequestBuilder = copy(method = "GET", url = url)
  def POST(url: String): RequestBuilder = copy(method = "POST", url = url)
  def POST(url: String, body: String): RequestBuilder = copy(method = "POST", url = url, body = Some(body))
  def PUT(url: String, body: String): RequestBuilder = copy(method = "PUT", url = url, body = Some(body))

  lazy val build: Request = Request(method, url, body)
}
