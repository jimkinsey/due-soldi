package hammerspace.markdown

import hammerspace.markdown.MarkdownParser.parseMarkdown
import utest._

object MarkdownToHtmlTests
extends TestSuite
{
  val tests = Tests {

    "rendering markdown as HTML" - {

      "for an unordered list" - {
        val markdown = parseMarkdown(
          s"""- a
             |- b
             |- c
           """.stripMargin)

        val html = MarkdownToHtml.html(markdown.nodes)

        assert(html == s"""<ul><li>a</li><li>b</li><li>c</li></ul>""")
      }

      "for an ordered list" - {
        val markdown = parseMarkdown(
          s"""1. a
             |2. b
             |3. c
           """.stripMargin)

        val html = MarkdownToHtml.html(markdown.nodes)

        assert(html == s"""<ol><li>a</li><li>b</li><li>c</li></ol>""")
      }

    }

  }
}
