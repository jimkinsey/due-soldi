package duesoldi.blog

import duesoldi.blog.model.BlogEntry

package object pages
{
  type MakeEntryPage = (String) => EntryPageMaker.Result
  type MakeIndexPage = () => IndexPageMaker.Result
  type BuildEntryPageModel = (BlogEntry) => BlogEntryPageModel
  type BuildIndexPageModel = Seq[BlogEntry] => BlogIndexPageModel

  type GetEntryTwitterMetadata = BlogEntry => Option[TwitterMetadata]
}
