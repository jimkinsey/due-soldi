package duesoldi.controller

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.extract
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.config.{Config, EnvironmentalConfig}

object RequestConfigDirective
{
  def requestConfig(appConfig: Config): Directive1[Config] = extract { implicit ctx: RequestContext =>
    (headerValue("Config-Override"), headerValue("Secret-Key")) match {
      case (Some(overrides), Some(key)) if appConfig.secretKey == key =>
        EnvironmentalConfig(toEnv(appConfig) ++ loggerName ++ nonSensitive(parse(overrides)))
      case _ =>
        appConfig
    }
  }

  private def loggerName(implicit ctx: RequestContext) =
    headerValue("Request-ID")
      .map(id => Map("LOGGER_NAME" -> s"Request $id"))
      .getOrElse(Map.empty)

  private def parse(str: String): Map[String, String] = {
    str.split(' ').map { case EnvVar(name, value) => name -> value } toMap
  }

  private lazy val EnvVar = """([A-Z_]+)=(.*)""".r

  private def headerValue(name: String)(implicit ctx: RequestContext): Option[String] =
    ctx.request.headers.find(_.name == name).map(_.value)
}
