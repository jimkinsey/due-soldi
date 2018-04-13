package duesoldi.app

import duesoldi.config.Config
import duesoldi.config.Config.Credentials
import sommelier.routing.Basic

object AdminAuth
{
  def basicAdminAuth(implicit config: Config): Basic = {
    val Credentials(username, password) = config.adminCredentials
    Basic(username, password, realm = "admin")
  }
}
