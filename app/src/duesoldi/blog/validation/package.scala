package duesoldi.blog

import hammerspace.markdown.MarkdownDocument

package object validation
{
  type ValidateIdentifier = (String) => Option[String]
  type ValidateContent = (MarkdownDocument) => Option[String]
}
