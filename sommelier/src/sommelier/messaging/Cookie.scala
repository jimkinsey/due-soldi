package sommelier.messaging

case class Cookie(name: String, value: String) {
  def format: String = s"$name=$value"
}
