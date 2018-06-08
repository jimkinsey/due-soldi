package sommelier.messaging

import ratatoskr.Cookie

case class Response(
  status: Int,
  body: Option[Array[Byte]] = None,
  headers: Seq[(String, String)] = Seq.empty
)
{
  def apply(body: String): Response = this(body.getBytes("UTF-8"))
  def apply(body: Array[Byte]): Response = copy(body = Some(body))
  def ContentType(contentType: String): Response = header("Content-Type" -> contentType)
  def Location(uri: String): Response = header("Location" -> uri)
  def WWWAuthenticate(auth: String): Response = header("WWW-Authenticate" -> auth)
  def body(body: String): Response = this(body)
  def body(body: Array[Byte]): Response = this(body)
  def header(header: (String, String)): Response = copy(headers = headers :+ header)
  def cookie(cookie: Cookie): Response = header("Set-Cookie" -> cookie.format)
  def body[T](implicit marshal: Array[Byte] => T): Option[T] = body map marshal

  override def equals(obj: scala.Any): Boolean = {
    def bodiesAreEqual(a: Option[Array[Byte]], b: Option[Array[Byte]]) = (a, b) match {
      case (Some(aBytes), Some(bBytes)) => java.util.Arrays.equals(aBytes, bBytes)
      case (None, None) => true
      case _ => false
    }

    obj match {
      case oth: Response =>
        oth.status == status && bodiesAreEqual(oth.body, body) && oth.headers == headers
      case _ => false
    }
  }

  override def toString: String = {
    s"""$status
       |${headers.map { case (key, value) => s"$key: $value" } mkString "\n"}
       |${body.map(bytes => new String(bytes, "utf-8")).getOrElse("")}""".stripMargin
  }
}
