package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import duesoldi.config.Config

object AdminAuthentication {

  def adminsOnly[T <: server.Route](credentials: Option[Config.Credentials])(block: => T): server.Route = authenticateBasic("admin", authenticatedAdminUser(credentials))(_ => block)

  private def authenticatedAdminUser(credentials: Option[Config.Credentials]): Authenticator[String] = {
    case providedPassword@Credentials.Provided(username) if isVerifiedAdmin(username, providedPassword, credentials) =>
      Some(username)
    case _ =>
      None
  }

  private def isVerifiedAdmin(providedUser: String, providedPassword: Provided, credentials: Option[Config.Credentials]) = credentials.exists { case Config.Credentials(username, password) =>
    providedUser == username && providedPassword.verify(password)
  }
}
