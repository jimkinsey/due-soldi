package duesoldi.controller

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import duesoldi.config.Config

object AdminAuthentication
{
  def adminsOnly(credentials: Config.Credentials): Directive[Unit] = {
    authenticateBasic("admin", authenticatedAdminUser(credentials)).flatMap(_=> Directive.Empty)
  }

  private def authenticatedAdminUser(credentials: Config.Credentials): Authenticator[String] = {
    case providedPassword@Credentials.Provided(username) if isVerifiedAdmin(username, providedPassword, credentials) =>
      Some(username)
    case _ =>
      None
  }

  private def isVerifiedAdmin(providedUser: String, providedPassword: Provided, credentials: Config.Credentials) =
    providedUser == credentials.username && providedPassword.verify(credentials.password)
}
