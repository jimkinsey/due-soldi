package object ratatoskr
{
  type Headers = Map[String, Seq[String]]
  val EmptyHeaders: Headers = Map.empty

  def addHeader(header: (String, String), headers: Headers): Headers = {
    val (name, value) = header
    headers.get(name) match {
      case Some(old) => headers ++ Map(name -> (old :+ value))
      case _ => headers ++ Map(name -> Seq(value))
    }
  }
}
