package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import duesoldi.config.{Config, Configured}

trait AdminAuthentication { self: Configured =>

  def adminsOnly[T <: server.Route](block: => T): server.Route = authenticateBasic("admin", authenticatedAdminUser)(_ => block)

  def authenticatedAdminUser: Authenticator[String] = {
    case providedPassword@Credentials.Provided(username) if isVerifiedAdmin(username, providedPassword) =>
      Some(username)
    case _ =>
      None
  }

  private def isVerifiedAdmin(providedUser: String, providedPassword: Provided) = config.adminCredentials.exists { case Config.Credentials(username, password) =>
    providedUser == username && providedPassword.verify(password)
  }

}
