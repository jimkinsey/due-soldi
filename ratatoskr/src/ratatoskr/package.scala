package object ratatoskr
{
  type Headers = Map[String, Seq[String]]
  val EmptyHeaders: Headers = Map.empty

  case class Request(method: Method, url: String, body: Stream[Byte] = Stream.empty, headers: Headers = EmptyHeaders)
  case class Response(status: Int, headers: Headers = EmptyHeaders, body: Stream[Byte] = Stream.empty)

  def addHeader(header: (String, String), headers: Headers): Headers = {
    val (name, value) = header
    headers.get(name) match {
      case Some(old) => headers ++ Map(name -> (old :+ value))
      case _ => headers ++ Map(name -> Seq(value))
    }
  }
}
