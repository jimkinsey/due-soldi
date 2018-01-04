package duesoldi.blog

import duesoldi.blog.model.BlogEntry

package object pages
{
  type MakeIndexPage = () => IndexPageMaker.Result
  type BuildEntryPageModel = (BlogEntry) => BlogEntryPageModel
  type BuildIndexPageModel = Seq[BlogEntry] => BlogIndexPageModel

  type GetEntryTwitterMetadata = BlogEntry => Option[TwitterMetadata]
}
