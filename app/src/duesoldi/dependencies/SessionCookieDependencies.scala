package duesoldi.dependencies

import duesoldi.app.sessions.Sessions
import duesoldi.app.sessions.Sessions.GetSessionCookie
import duesoldi.dependencies.Injection.Inject

trait SessionCookieDependencies {
  implicit lazy val getSessionCookie: Inject[GetSessionCookie] = config => Sessions.getSessionCookie(config.secretKey)
}
