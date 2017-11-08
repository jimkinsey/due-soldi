package duesoldi.page

import duesoldi.controller.DebugRoutes.MakeHeadersPage

object HeadersPageMaker
{
  def makeHeadersPage: MakeHeadersPage =
    _.headers.map { header => s"${header.name}: ${header.value}" } mkString "\n"
}
