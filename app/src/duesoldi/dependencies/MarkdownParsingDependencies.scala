package duesoldi.dependencies

import duesoldi.dependencies.Injection.Inject
import hammerspace.markdown.MarkdownParser

trait MarkdownParsingDependencies {
  implicit lazy val parseMarkdown: Inject[hammerspace.markdown.Parse] = _ => MarkdownParser.parseMarkdown
}
