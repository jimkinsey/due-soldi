package duesoldi.validation

object ValidIdentifier {
  val Valid = """^([a-z0-9\-]+)$""".r
  def apply(identifier: String): Option[String] = Valid.findFirstIn(identifier)
}
