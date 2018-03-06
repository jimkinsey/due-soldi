package duesoldi.test.unit

import duesoldi.markdown.MarkdownDocument._
import duesoldi.markdown.MarkdownParser
import utest._
import MarkdownParser.parseMarkdown

object MarkdownParserTests
extends TestSuite
{
  val tests = this
  {
    'level_headings
    {
      (1 to 6).foreach { level =>
        assert(parseMarkdown(s"${"#" * level} Title").nodes == Seq(Heading(Seq(Text("Title")), level)))
      }
    }
    'heading_emphasis
    {
      assert(parseMarkdown(s"## _emphasising_ things").nodes == Seq(Heading(Seq(Emphasis("emphasising"), Text(" things")), 2)))
    }
    'emphasis
    {
      assert(parseMarkdown("_emphasised_").nodes == Seq(Paragraph(Seq(Emphasis("emphasised")))))
    }
    'strong_emphasis
    {
      assert(parseMarkdown("**emphasised**").nodes == Seq(Paragraph(Seq(Strong("emphasised")))))
    }
    'paragraph_text
    {
      assert(parseMarkdown("some text").nodes == Seq(Paragraph(Seq(Text("some text")))))
    }
    'code_blocks
    {
      val actual = parseMarkdown(
        """    10 PRINT "HELLO"
          |    20 GOTO 10"""".stripMargin).nodes.toList
      val expected = Seq(Code(
        """10 PRINT "HELLO"
          |20 GOTO 10"""".stripMargin))
      assert(actual == expected)
    }
    'inline_code
    {
      assert(parseMarkdown("`markdown(str: String)`").nodes == Seq(Paragraph(Seq(InlineCode("markdown(str: String)")))))
    }
    'inline_links
    {
      assert(
        parseMarkdown("This is [an example](http://example.com/ \"Title\") inline link.").nodes == {
          Seq(Paragraph(Seq(Text("This is "), InlineLink("an example", "http://example.com/", Some("Title")), Text(" inline link."))))
        }
      )
    }
    'unordered_lists
    {
      assert(
        parseMarkdown(
          """ * one
            | * two
            | * three
          """.stripMargin).nodes
          == {
          Seq(UnorderedList(Seq(
            Seq(Text("one")),
            Seq(Text("two")),
            Seq(Text("three"))
          )))
        }
      )
    }
    'ordered_lists
    {
      assert(
        parseMarkdown(
          """ 1. A
            | 2. B
            | 3. C
          """.stripMargin).nodes == {
          Seq(OrderedList(Seq(
            Seq(Text("A")),
            Seq(Text("B")),
            Seq(Text("C"))
          )))
        }
      )
    }
    'block_quotes
    {
      assert(
        parseMarkdown(
          """> Good morning.
            |>
            |> In less than an hour, aircraft from here will join others from around the world. And you will be launching the largest aerial battle in the history of mankind.
            |>
            |> “Mankind.” That word should have new meaning for all of us today.
          """.stripMargin
        ).nodes == {
          Seq(BlockQuote(Seq(
            Paragraph(Seq(Text("Good morning."))),
            Paragraph(Seq(Text("In less than an hour, aircraft from here will join others from around the world. And you will be launching the largest aerial battle in the history of mankind."))),
            Paragraph(Seq(Text("“Mankind.” That word should have new meaning for all of us today.")))
          )))
        }
      )
    }
    'line_breaks
    {
      assert(
        parseMarkdown(
          """10: PRINT "Hello"
            |20: GOTO 10""".stripMargin).nodes == {
          Seq(Paragraph(Seq(Text("""10: PRINT "Hello""""), LineBreak, Text("""20: GOTO 10"""))))
        }
      )
    }
    'horizontal_rules
    {
      assert(parseMarkdown("***").nodes == Seq(HorizontalRule))
    }
    'images
    {
      assert(
        parseMarkdown("![Alt text](/path/to/img.jpg \"Optional title\")").nodes == {
          Seq(Paragraph(Seq(Image(alt = "Alt text", src = "/path/to/img.jpg", title = Some("Optional title")))))
        }
      )
    }
    'include_raw_source
    {
      assert(parseMarkdown("# here me raw").raw == "# here me raw")
    }
  }
}
