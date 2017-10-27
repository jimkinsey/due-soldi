package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.{AuthenticationDirective, Credentials}
import akka.http.scaladsl.server.directives.Credentials.Provided
import duesoldi.config.Config

object AdminAuthentication
{
  def adminsOnly(credentials: Option[Config.Credentials]): AuthenticationDirective[String] = {
    authenticateBasic("admin", authenticatedAdminUser(credentials))
  }

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
