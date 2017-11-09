package duesoldi.debug.pages

object HeadersPageMaker
{
  def makeHeadersPage: MakeHeadersPage =
    _.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
}
