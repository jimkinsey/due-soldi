package ratatoskr

case class Cookie(name: String, value: String)
{
  def format: String = s"$name=$value"
  def toRequestHeader: (String, String) = "Cookie" -> format
}

object Cookie
{
  def parse(string: String): Cookie = string match {
    case Valid(name, value) => Cookie(name, value)
  }

  private val Valid = """^(\w+)=(.*)$""".r // FIXME broken
}