package duesoldi.blog

import duesoldi.blog.model.BlogEntry

package object pages
{
  type BuildEntryPageModel = (BlogEntry) => BlogEntryPageModel
  type BuildIndexPageModel = Seq[BlogEntry] => BlogIndexPageModel

  type GetEntryTwitterMetadata = BlogEntry => Option[TwitterMetadata]
}
