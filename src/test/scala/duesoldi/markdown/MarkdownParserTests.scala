package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

// TODO handle all MD tags
// TODO check whether strong, emphasis, etc. can have child tags
class MarkdownParserTests extends WordSpec {

  "A markdown parser" must {

    "include level 1 headings" in {
      parser.markdown("# Title") shouldBe MarkdownDocument(Seq(Heading("Title", level = 1)))
    }

    "handle emphasis" in {
      parser.markdown("_emphasised_") shouldBe MarkdownDocument(Seq(Paragraph(Seq(Emphasis("emphasised")))))
    }

    "handle strong emphasis" in {
      parser.markdown("**emphasised**") shouldBe MarkdownDocument(Seq(Paragraph(Seq(Strong("emphasised")))))
    }

    "handle paragraph text" in {
      parser.markdown("some text") shouldBe MarkdownDocument(Seq(Paragraph(Seq(Text("some text")))))
    }

  }

  private lazy val parser = new MarkdownParser
}
