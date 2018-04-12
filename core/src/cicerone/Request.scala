package cicerone

case class Request(method: String, url: String, body: Option[String] = None, headers: Headers = Map.empty)

case class RequestBuilder(method: String = "GET", url: String = "http://localhost", body: Option[String] = None, headers: Headers = Map.empty)
{
  def GET(url: String): RequestBuilder = copy(method = "GET", url = url)
  def HEAD(url: String): RequestBuilder = copy(method = "HEAD", url = url)
  def POST(url: String): RequestBuilder = copy(method = "POST", url = url)
  def POST(url: String, body: String): RequestBuilder = copy(method = "POST", url = url, body = Some(body))
  def PUT(url: String, body: String): RequestBuilder = copy(method = "PUT", url = url, body = Some(body))
  def DELETE(url: String): RequestBuilder = copy(method = "DELETE", url = url)

  def header(header: (String, String)): RequestBuilder = {
    headers.get(header._1) match {
      case None => copy(headers = headers ++ Map(header._1 -> Seq(header._2)))
      case Some(values) => copy(headers = headers ++ Map(header._1 -> (values :+ header._2)))
    }
  }

  lazy val build: Request = Request(method, url, body, headers)
}
