package duesoldi

import scala.concurrent.Future

package object rendering {
  type Render = (String, PageModel) => Future[bhuj.Result]
}
