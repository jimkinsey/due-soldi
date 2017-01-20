package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument.Heading
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class MarkdownParserTests extends WordSpec {

  "A markdown parser" must {

    "include level 1 headings" in {
      val markdown = "# Title"
      val parser = new MarkdownParser
      parser.markdown(markdown) shouldBe MarkdownDocument(Seq(Heading("Title", level = 1)))
    }

  }

}
