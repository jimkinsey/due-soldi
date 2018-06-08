package ratatoskr

case class Cookie(name: String, value: String) {
  def toRequestHeader: (String, String) = "Cookie" -> s"$name=$value"
}
