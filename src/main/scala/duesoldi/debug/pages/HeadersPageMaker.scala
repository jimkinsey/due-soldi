package duesoldi.debug.pages

object HeadersPageMaker
{
  def makeHeadersPage: MakeHeadersPage =
    _.headers.map { case (name, values) => values.map(value => s"$name: $value").mkString("\n") } mkString "\n"
}
