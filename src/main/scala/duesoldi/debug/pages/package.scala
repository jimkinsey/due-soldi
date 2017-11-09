package duesoldi.debug

import akka.http.scaladsl.model.HttpRequest

package object pages
{
  type MakeHeadersPage = HttpRequest => String
  type MakeConfigPage = () => String
}
