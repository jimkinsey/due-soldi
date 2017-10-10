package duesoldi

import scala.concurrent.Future

package object rendering {
  type Rendered = (String, PageModel) => Future[bhuj.Result]
}
