package duesoldi.app

import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.config.{Config, EnvironmentalConfig}
import sommelier._
import sommelier.handling.Unpacking._
import sommelier.routing.Result
import sommelier.routing.Routing._

object RequestConfig
{
  def requestConfig(implicit appConfig: Config, context: Context): Result[Config] =
    for {
      configOverride <- header("Config-override")
      _ <- header("Secret-key").validate(_ == appConfig.secretKey) { 403 ("") }
      requestId <- header("Request-ID") recover { _ => "n/a" }
      loggerName = Map("LOGGER_NAME" -> s"Request $requestId")
    } yield {
      EnvironmentalConfig(toEnv(appConfig) ++ loggerName ++ nonSensitive(Config.parse(configOverride)))
    }
}

