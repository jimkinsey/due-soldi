package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import duesoldi.config.Configured

trait MetricsRoutes { self: Configured =>

  final def metricsRoutes =
    path("admin" / "metrics" / "access.csv") {
      authenticateBasic("admin", authenticatedAdminUser) { username =>
        complete {
          "Timestamp,Path" // ,Referer,User-agent,Duration,Status code,IP
        }
      }
    }


  def authenticatedAdminUser: Authenticator[String] = {
    case password @ Credentials.Provided(username) if config.adminCredentials.exists(creds => password.verify(creds.password)) => Some(username)
    case _ => None
  }

}
