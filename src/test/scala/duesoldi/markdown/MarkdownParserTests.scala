package duesoldi.markdown

import duesoldi.markdown.MarkdownDocument._
import utest._

object MarkdownParserTests
extends TestSuite
{
  val tests = this
  {
    'level_headings
    {
      (1 to 6).foreach { level =>
        assert(parser.markdown(s"${"#" * level} Title").nodes == Seq(Heading(Seq(Text("Title")), level)))
      }
    }
    'heading_emphasis
    {
      assert(parser.markdown(s"## _emphasising_ things").nodes == Seq(Heading(Seq(Emphasis("emphasising"), Text(" things")), 2)))
    }
    'emphasis
    {
      assert(parser.markdown("_emphasised_").nodes == Seq(Paragraph(Seq(Emphasis("emphasised")))))
    }
    'strong_emphasis
    {
      assert(parser.markdown("**emphasised**").nodes == Seq(Paragraph(Seq(Strong("emphasised")))))
    }
    'paragraph_text
    {
      assert(parser.markdown("some text").nodes == Seq(Paragraph(Seq(Text("some text")))))
    }
    'code_blocks
    {
      assert(parser.markdown("    parser.markdown(").nodes == Seq(Code("parser.markdown(")))
    }
    'inline_code
    {
      assert(parser.markdown("`markdown(str: String)`").nodes == Seq(Paragraph(Seq(InlineCode("markdown(str: String)")))))
    }
    'inline_links
    {
      assert(
        parser.markdown("This is [an example](http://example.com/ \"Title\") inline link.").nodes == {
          Seq(Paragraph(Seq(Text("This is "), InlineLink("an example", "http://example.com/", Some("Title")), Text(" inline link."))))
        }
      )
    }
    'unordered_lists
    {
      assert(
        parser.markdown(
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
        parser.markdown(
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
        parser.markdown(
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
        parser.markdown(
          """10: PRINT "Hello"
            |20: GOTO 10""".stripMargin).nodes == {
          Seq(Paragraph(Seq(Text("""10: PRINT "Hello""""), LineBreak, Text("""20: GOTO 10"""))))
        }
      )
    }
    'horizontal_rules
    {
      assert(parser.markdown("***").nodes == Seq(HorizontalRule))
    }
    'images
    {
      assert(
        parser.markdown("![Alt text](/path/to/img.jpg \"Optional title\")").nodes == {
          Seq(Paragraph(Seq(Image(alt = "Alt text", src = "/path/to/img.jpg", title = Some("Optional title")))))
        }
      )
    }
    'include_raw_source
    {
      assert(parser.markdown("# here me raw").raw == "# here me raw")
    }
  }

  private lazy val parser = new MarkdownParser
}
