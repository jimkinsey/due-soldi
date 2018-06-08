package hammerspace.uri

import utest._

object URITests
extends TestSuite
{
  val tests = this {
    "A URI" - {
      "has a path" - {
        val path = URI.parse("http://link.springer.com/journal/11464").path
        assert(path == "/journal/11464")
      }
      "has a query string" - {
        val queryString = URI.parse("http://link.springer.com/search?facet-content-type=Book").queryString
        assert(queryString contains "facet-content-type=Book")
      }
      "sometimes does not have a query string" - {
        val queryString = URI.parse("http://link.springer.com").queryString
        assert(queryString isEmpty)
      }
      "allows the addition of a query string parameter to an empty params URI" - {
        val queryString = URI.parse("http://link.springer.com/search")
          .addParam("facet-content-type" -> "Book").queryString
        assert(queryString contains "facet-content-type=Book")
      }
      "allows the addition of a query string parameter to a URI which already has parameters" - {
        val queryString = URI.parse("http://link.springer.com/search?facet-content-type=Chapter")
          .addParam("query" -> "stuff").queryString
        assert(queryString contains "facet-content-type=Chapter&query=stuff")
      }
      "can be formatted to a string" - {
        val formatted = URI(
          scheme = "http",
          authority = "authority",
          path = "/path",
          queryString = Some("query=yes"),
          fragment = Some("fragment")
        ).format
        assert(formatted == "http://authority/path?query=yes#fragment")
      }
    }
  }
}