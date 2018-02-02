package duesoldi

import duesoldi.blog.pages.PageModel

import scala.concurrent.Future

package object rendering {
  type Render = (String, PageModel) => Future[bhuj.Result]
}
