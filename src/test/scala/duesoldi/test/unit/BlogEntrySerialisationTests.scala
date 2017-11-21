package duesoldi.test.unit

import java.time.format.DateTimeFormatter

import duesoldi.blog.serialisation.EntryYaml
import duesoldi.test.support.matchers.CustomMatchers._
import utest._

object BlogEntrySerialisationTests
extends TestSuite
{
  val tests = this
  {
    "Parsing Blog Entry YAML" - {
      "results in a parse failure when the content is missing" - {
        val parseResult = EntryYaml.parse(
          """id: '123'
            |description:
            |last-modified:
          """.stripMargin
        )
        assert(parseResult isLeftOf EntryYaml.ParseFailure.MissingContent)
      }
      "results in a parse failure when the ID is missing" - {
        val parseResult = EntryYaml.parse(
          """description:
            |last-modified:
            |content: |
            |  # Title
          """.stripMargin
        )
        assert(parseResult isLeftOf EntryYaml.ParseFailure.MissingId)
      }
      "results in a blog entry with the ID taken from the id field" - {
        val parseResult = EntryYaml.parse(
          """id: hello
            |description:
            |last-modified:
            |content: |
            |  # Title
          """.stripMargin
        )
        assert(parseResult exists (_.id == "hello"))
      }
      "results in a blog entry with the description when defined" - {
        val parseResult = EntryYaml.parse(
          """id: hello
            |description: Some top class content
            |last-modified:
            |content: |
            |  # Title
          """.stripMargin
        )
        assert(parseResult exists (_.description.contains("Some top class content")))
      }
      "results in a blog entry with no description when not defined" - {
        val parseResult = EntryYaml.parse(
          """id: hello
            |description:
            |last-modified:
            |content: |
            |  # Title
          """.stripMargin
        )
        assert(parseResult exists (_.description.isEmpty))
      }
      "results in a blog entry with the specified last modified when defined" - {
        val parseResult = EntryYaml.parse(
          """id: hello
            |description:
            |last-modified: 2010-10-12T16:05:00+01:00
            |content: |
            |  # Title
          """.stripMargin
        )
        val lastModified = parseResult.toOption.map(_.lastModified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
        assert(lastModified contains "2010-10-12T16:05:00+01:00")
      }
    }
  }
}
