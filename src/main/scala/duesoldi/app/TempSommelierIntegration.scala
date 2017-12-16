package duesoldi.app

import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}
import duesoldi.config.{Config, EnvironmentalConfig}
import duesoldi.dependencies.Injection.Inject
import sommelier.routing.Result

object TempSommelierIntegration // FIXME
{
  import sommelier.routing.Routing._
  import sommelier.handling.Unpacking._
  import sommelier._

  def requestConfig(implicit appConfig: Config, context: Context): Result[Config] =
    for {
      // todo header should just return first value
      configOverride <- header("Config-override") map (_.head)
      _ <- header("Secret-key").map(_.head).validate(_ == appConfig.secretKey) { 403 ("") }
      requestId <- header("Request-ID") map (_.head) recover { _ => "n/a" }
      loggerName = Map("LOGGER_NAME" -> s"Request $requestId")
    } yield {
      EnvironmentalConfig(toEnv(appConfig) ++ loggerName ++ nonSensitive(Config.parse(configOverride)))
    }

  def provided[T](implicit injector: Inject[T], appConfig: Config, context: Context): Result[T] =
    requestConfig recover (_ => appConfig) map injector

  def provided[T1,T2,T3,T4](implicit injector1: Inject[T1], injector2: Inject[T2], injector3: Inject[T3], injector4: Inject[T4], appConfig: Config, context: Context): Result[(T1,T2,T3,T4)] =
    requestConfig recover (_ => appConfig) map { config =>
      (injector1(config), injector2(config), injector3(config), injector4(config))
    }
}
