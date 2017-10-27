package duesoldi.controller

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.config.{Config, EnvironmentalConfig}

object OverrideConfigDirective
{
  def overrideConfig(appConfig: Config): Directive1[Config] = extract { implicit ctx =>
    (headerValue("Config-Override"), headerValue("Secret-Key")) match {
      case (Some(overrides), Some(key)) if appConfig.secretKey == key =>
        EnvironmentalConfig(toEnv(appConfig) ++ nonSensitive(parse(overrides)))
      case _ =>
        appConfig
    }
  }

  private def parse(str: String): Map[String, String] = {
    str.split(' ').map { case EnvVar(name, value) => name -> value } toMap
  }

  private lazy val EnvVar = """([A-Z_]+)=(.*)""".r

  private def headerValue(name: String)(implicit ctx: server.RequestContext): Option[String] =
    ctx.request.headers.find(_.name == name).map(_.value)
}
