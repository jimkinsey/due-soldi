package duesoldi

import duesoldi.blog.model.BlogEntry
import duesoldi.blog.serialisation.EntryYaml

package object blog
{
  type EntryFromYaml = String => Either[EntryYaml.ParseFailure, BlogEntry]
  type EntryToYaml = BlogEntry => String
}
