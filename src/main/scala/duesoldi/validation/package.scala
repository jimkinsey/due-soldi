package duesoldi

import duesoldi.markdown.MarkdownDocument

package object validation {
  type ValidIdentifier = (String) => Option[String]
  type ValidBlogContent = (MarkdownDocument) => Option[String]
}
