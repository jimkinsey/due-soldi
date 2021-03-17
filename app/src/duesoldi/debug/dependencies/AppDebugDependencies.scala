package duesoldi.debug.dependencies

import duesoldi.debug.pages.{ConfigPageMaker, HeadersPageMaker, MakeConfigPage, MakeHeadersPage}
import duesoldi.dependencies.Injection.Inject

trait AppDebugDependencies {

  implicit def makeHeadersPage: Inject[MakeHeadersPage] = _ => HeadersPageMaker.makeHeadersPage

  implicit def makeConfigPage: Inject[MakeConfigPage] = ConfigPageMaker.makeConfigPage

}
