package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

// TODO handle all MD tags
class MarkdownParserTests extends WordSpec {

  "A markdown parser" must  {

    "handle all level headings" in {
      (1 to 6).foldLeft(succeed) { case (_, level) =>
        parser.markdown(s"${"#" * level} Title") shouldBe MarkdownDocument(Seq(Heading("Title", level)))
      }
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

    "handle code blocks" in {
      parser.markdown("    parser.markdown(") shouldBe MarkdownDocument(Seq(Code("parser.markdown(")))
    }

    "handle inline links" in {
      parser.markdown("This is [an example](http://example.com/ \"Title\") inline link.") shouldBe {
        MarkdownDocument(Seq(Paragraph(Seq(Text("This is "), InlineLink("an example", "http://example.com/", Some("Title")), Text(" inline link.")))))
      }
    }

    "handle unordered lists" in {
      parser.markdown(
        """ * one
          | * two
          | * three
        """.stripMargin) shouldBe {
        MarkdownDocument(Seq(UnorderedList(Seq(
          Seq(Text("one")),
          Seq(Text("two")),
          Seq(Text("three"))
        ))))
      }
    }

    "handle ordered lists" in {
      parser.markdown(
        """ 1. A
          | 2. B
          | 3. C
        """.stripMargin) shouldBe {
        MarkdownDocument(Seq(OrderedList(Seq(
          Seq(Text("A")),
          Seq(Text("B")),
          Seq(Text("C"))
        ))))
      }

    }

  }

  private lazy val parser = new MarkdownParser
}
